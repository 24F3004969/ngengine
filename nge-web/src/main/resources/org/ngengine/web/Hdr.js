/**
 * Based on https://enkimute.github.io/hdrpng.js/ by enki:
            MIT License

            Copyright (c) 2017 enki

            Permission is hereby granted, free of charge, to any person obtaining a copy
            of this software and associated documentation files (the "Software"), to deal
            in the Software without restriction, including without limitation the rights
            to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
            copies of the Software, and to permit persons to whom the Software is
            furnished to do so, subject to the following conditions:

            The above copyright notice and this permission notice shall be included in all
            copies or substantial portions of the Software.

            THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
            IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
            FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
            AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
            LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
            OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
            SOFTWARE.
*/


 

 
/** Load and parse a Radiance .HDR file. It completes with a 32bit RGBE buffer.
  * @param {URL} url location of .HDR file to load.
  * @param {function} completion completion callback.
  * @returns {XMLHttpRequest} the XMLHttpRequest used to download the file.
  */
export function loadHDR(data) {
    let header = '', pos = 0, d8 = new Uint8Array(data), format;
    // read header.  
    while (!header.match(/\n\n[^\n]+\n/g)) header += String.fromCharCode(d8[pos++]);
    // check format. 
    format = header.match(/FORMAT=(.*)$/m)[1];
    if (format != '32-bit_rle_rgbe') return console.warn('unknown format : ' + format), this.onerror();
    // parse resolution
    let rez = header.split(/\n/).reverse()[1].split(' '), width = rez[3] * 1, height = rez[1] * 1;
    // Create image.
    let img = new Uint8Array(width * height * 4), ipos = 0;
    // Read all scanlines
    for (let j = 0; j < height; j++) {
        let rgbe = d8.slice(pos, pos += 4), scanline = [];
        if (rgbe[0] != 2 || (rgbe[1] != 2) || (rgbe[2] & 0x80)) {
            let len = width, rs = 0; pos -= 4; while (len > 0) {
                img.set(d8.slice(pos, pos += 4), ipos);
                if (img[ipos] == 1 && img[ipos + 1] == 1 && img[ipos + 2] == 1) {
                    for (img[ipos + 3] << rs; i > 0; i--) {
                        img.set(img.slice(ipos - 4, ipos), ipos);
                        ipos += 4;
                        len--
                    }
                    rs += 8;
                } else { len--; ipos += 4; rs = 0; }
            }
        } else {
            if ((rgbe[2] << 8) + rgbe[3] != width) return console.warn('HDR line mismatch ..'), this.onerror();
            for (let i = 0; i < 4; i++) {
                let ptr = i * width, ptr_end = (i + 1) * width, buf, count;
                while (ptr < ptr_end) {
                    buf = d8.slice(pos, pos += 2);
                    if (buf[0] > 128) { count = buf[0] - 128; while (count-- > 0) scanline[ptr++] = buf[1]; }
                    else { count = buf[0] - 1; scanline[ptr++] = buf[1]; while (count-- > 0) scanline[ptr++] = d8[pos++]; }
                }
            }
            for (let i = 0; i < width; i++) { img[ipos++] = scanline[i]; img[ipos++] = scanline[i + width]; img[ipos++] = scanline[i + 2 * width]; img[ipos++] = scanline[i + 3 * width]; }
        }
    }
      
    return {
        data: rgbeToFloat(img),
        width,
        height
    }
}
 

 

/** Convert an RGBE buffer to a Float buffer.
  * @param {Uint8Array} buffer The input buffer in RGBE format. (as returned from loadHDR)
  * @param {Float32Array} [res] Optional result buffer containing 3 floats per pixel.
  * @returns {Float32Array} A floating point buffer with 96 bits per pixel (32 per channel, 3 channels).
  */
export function rgbeToFloat(buffer) {
    let s, l = buffer.byteLength >> 2;
    const res =   new Float32Array(l * 3);
    for (var i = 0; i < l; i++) {
        s = Math.pow(2, buffer[i * 4 + 3] - (128 + 8));
        res[i * 3] = buffer[i * 4] * s;
        res[i * 3 + 1] = buffer[i * 4 + 1] * s;
        res[i * 3 + 2] = buffer[i * 4 + 2] * s;
    }
    return res;
}
 