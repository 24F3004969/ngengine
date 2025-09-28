package org.ngengine.web.audio;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.teavm.jso.core.JSArray;
import org.teavm.jso.typedarrays.Float32Array;
import org.teavm.jso.typedarrays.Int8Array;

import com.jme3.audio.AudioBuffer;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioStream;
import com.jme3.audio.Environment;
import com.jme3.util.res.Resources;

public class WebAudioDataUtils {
    private static void reverseOrder(byte[] bytes) {
        for (int i = 0; i < bytes.length / 2; i++) {
            byte temp = bytes[i];
            bytes[i] = bytes[bytes.length - i - 1];
            bytes[bytes.length - i - 1] = temp;
        }
    }

    private static void write(byte[] channelSample, int j, int bps, boolean swapOrder, Float32Array out) {
        if (swapOrder) reverseOrder(channelSample);
        double dcOffset = 0;
        if (bps == 8) {
            byte n = channelSample[0];
            float fbe;
            if (n < 0) {
                fbe = (float) ((((double) n) - dcOffset) / 128.);
            } else {
                fbe = (float) ((((double) n) + dcOffset) / 127.);
            }
            out.set(j, fbe);
        } else if (bps == 16) {
            short sbe = (short) ((channelSample[1] & 0xFF) << 8 | (channelSample[0] & 0xFF));
            float fbe;
            if (sbe < 0) {
                fbe = (float) ((((double) sbe) - dcOffset) / 32768.);
            } else {
                fbe = (float) ((((double) sbe) + dcOffset) / 32767.);
            }
            out.set(j, fbe);
        } else if (bps == 24) {
            int ibe = (int) ((channelSample[2] & 0xFF) << 16 | (channelSample[1] & 0xFF) << 8
                    | (channelSample[0] & 0xFF));
            // Extend sign to int32
            if ((ibe & 0x00800000) > 0) ibe |= 0xFF000000;

            float fbe;
            if (ibe < 0) {
                fbe = (float) ((((double) ibe) - dcOffset) / 8388608.);
            } else {
                fbe = (float) ((((double) ibe) + dcOffset) / 8388607.);
            }
            out.set(j, fbe);
        } else {
            throw new UnsupportedOperationException("Unsupported bits per sample: " + bps);
        }
    }

    private static void audioDataToF32(AudioData ab, ByteBuffer in, JSArray<Float32Array> outs, int srcSampleRate,
            int dstSampleRate) {
        int bps = ab.getBitsPerSample();
        int channels = ab.getChannels();
        byte channelSample[] = new byte[bps / 8];
        boolean swapOrder = in.order() != ByteOrder.nativeOrder();

        double samplePos = 0;
        double sampleInc = (double) srcSampleRate / (double) dstSampleRate;

        for (int outPos = 0; outPos < outs.get(0).getLength(); outPos++) {
            for (int c = 0; c < channels; c++) { // interleaved

                int pos = (int) (samplePos) * channels * channelSample.length;
                pos += c * channelSample.length;

                in.position(pos);
                in.get(channelSample);

                write(channelSample, outPos, bps, swapOrder, outs.get(c));
            }
            samplePos += sampleInc;
        }
    }

    private static JSArray<Float32Array> getF32Data(AudioStream ab, int srcSampleRate, int destSampleRate, int lengthInSamples) {
        ByteBuffer inputData = ByteBuffer.allocateDirect(lengthInSamples * (ab.getBitsPerSample() / 8));
        byte chunk[] = new byte[1024];
        int read = 0;
        while ((read = ab.readSamples(chunk)) > 0) {
            inputData.put(chunk, 0, read);
        }
        inputData.rewind();
        // Float32Array data[] = new Float32Array[ab.getChannels()];
        JSArray<Float32Array> data = new JSArray<>(ab.getChannels());
        for (int i = 0; i < ab.getChannels(); i++) {
            data.set(i, new Float32Array(lengthInSamples));
        }
        audioDataToF32(ab, inputData, data, srcSampleRate, destSampleRate);
        inputData.rewind();
        return data;

    }

    private static JSArray<Float32Array>  getF32Data(AudioBuffer ab, int srcSampleRate, int destSampleRate, int lengthInSamples) {
        ByteBuffer inputData = ab.getData();
        inputData.rewind();
        JSArray<Float32Array> data = new JSArray<>(ab.getChannels());
         for (int i = 0; i < ab.getChannels(); i++) {
            data.set(i, new Float32Array(lengthInSamples));
        }        
        audioDataToF32(ab, inputData, data, srcSampleRate, destSampleRate);
        inputData.rewind();
        return data;
    }

    public static JSArray<Float32Array> getF32Data(AudioData ab, int destSampleRate) {
        int srcSampleRate = ab.getSampleRate();
        int lengthInSamples = (int) (ab.getDuration() * ab.getSampleRate());
        if (ab instanceof AudioStream) {
            return getF32Data((AudioStream) ab, srcSampleRate, destSampleRate, lengthInSamples);
        } else if (ab instanceof AudioBuffer) {
            return getF32Data((AudioBuffer) ab, srcSampleRate, destSampleRate, lengthInSamples);
        } else {
            throw new UnsupportedOperationException("Unsupported AudioData type: " + ab.getClass().getName());
        }
    }
  public static Int8Array getEnvData(Environment currentEnv) {
            String impulseName = "";
            if (currentEnv == null) throw new RuntimeException("No environment set");
            if (currentEnv == Environment.AcousticLab) {
                impulseName = "Basement.m4a";
            } else if (currentEnv == Environment.Cavern) {
                impulseName = "EmptyApartmentBedroom.m4a";
            } else if (currentEnv == Environment.Closet) {
                impulseName = "Basement.m4a";
            } else if (currentEnv == Environment.Dungeon) {
                impulseName = "PurnodesRailroadTunnel.m4a";
            } else if (currentEnv == Environment.Garage) {
                impulseName = "Basement.m4a";
            }
            InputStream is = Resources.getResourceAsStream("org/ngengine/web/impulse/"+impulseName);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte buff[] = new byte[1024];
            int read = 0;
            try {
                while ((read = is.read(buff)) != -1) {
                    baos.write(buff, 0, read);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            buff = baos.toByteArray();
            Int8Array i8 = new Int8Array(buff.length);
            for (int i = 0; i < buff.length; i++) {
                i8.set(i, buff[i]);
            }          
            return i8;
        }
}
