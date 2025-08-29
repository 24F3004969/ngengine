/*
 * Copyright (c) 2015-2022 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.anim.tween;

import com.jme3.anim.util.Primitives;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Static utility methods for creating common generic Tween objects.
 *
 * @author Paul Speed
 */
public class Tweens {

    private static final Logger log = Logger.getLogger(Tweens.class.getName());

    private static final CurveFunction SMOOTH = new SmoothStep();
    private static final CurveFunction SINE = new Sine();

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private Tweens() {
    }

    /**
     * Creates a tween that will interpolate over an entire sequence
     * of tweens in order.
     *
     * @param delegates the desired sequence of tweens
     * @return a new instance
     */
    public static Tween sequence(Tween... delegates) {
        return new Sequence(delegates);
    }

    /**
     * Creates a tween that will interpolate over an entire list
     * of tweens in parallel, ie: all tweens will be run at the same
     * time.
     *
     * @param delegates the tweens to be interpolated
     * @return a new instance
     */
    public static Tween parallel(Tween... delegates) {
        return new Parallel(delegates);
    }

    /**
     * Creates a tween that will perform a no-op until the length
     * has expired.
     *
     * @param length the desired duration (in seconds)
     * @return a new instance
     */
    public static Tween delay(double length) {
        return new Delay(length);
    }

    /**
     * Creates a tween that scales the specified delegate tween or tweens
     * to the desired length.  If more than one tween is specified then they
     * are wrapped in a sequence using the sequence() method.
     *
     * @param desiredLength the desired duration (in seconds)
     * @param delegates the desired sequence of tweens
     * @return a new instance
     */
    public static Tween stretch(double desiredLength, Tween... delegates) {
        if (delegates.length == 1) {
            return new Stretch(delegates[0], desiredLength);
        }
        return new Stretch(sequence(delegates), desiredLength);
    }

    /**
     * Creates a tween that uses a sine function to smooth step the time value
     * for the specified delegate tween or tweens.  These 'curved' wrappers
     * can be used to smooth the interpolation of another tween.
     *
     * @param delegates the desired sequence of tweens
     * @return a new instance
     */
    public static Tween sineStep(Tween... delegates) {
        if (delegates.length == 1) {
            return new Curve(delegates[0], SINE);
        }
        return new Curve(sequence(delegates), SINE);
    }

    /**
     * Creates a tween that uses a hermite function to smooth step the time value
     * for the specified delegate tween or tweens.  This is similar to GLSL's
     * smoothstep().  These 'curved' wrappers can be used to smooth the interpolation
     * of another tween.
     *
     * @param delegates the desired sequence of tweens
     * @return a new instance
     */
    public static Tween smoothStep(Tween... delegates) {
        if (delegates.length == 1) {
            return new Curve(delegates[0], SMOOTH);
        }
        return new Curve(sequence(delegates), SMOOTH);
    }

    /**
     * Creates a tween that loops the specified delegate tween or tweens
     * to the desired count.  If more than one tween is specified then they
     * are wrapped in a sequence using the sequence() method.
     *
     * @param count the desired loop count
     * @param delegates the desired sequence of tweens
     * @return a new instance
     */
    public static Tween loopCount(int count, Tween... delegates) {
        if (delegates.length == 1) {
            return new Loop(delegates[0], count);
        }

        return new Loop(sequence(delegates), count);
    }

    /**
     * Creates a tween that loops the specified delegate tween or tweens
     * to the desired duration.  If more than one tween is specified then they
     * are wrapped in a sequence using the sequence() method.
     *
     * @param duration the desired duration
     * @param delegates the desired sequence of tweens
     * @return a new instance
     */
    public static Tween loopDuration(double duration, Tween... delegates) {
        if (delegates.length == 1) {
            return new Loop(delegates[0], duration);
        }

        return new Loop(sequence(delegates), duration);
    }

    /**
     * Creates a tween that inverts the specified delegate tween.
     *
     * @param delegate the desired tween
     * @return a new instance
     */
    public static Tween invert(Tween delegate) {
        return new Invert(delegate);
    }

    /**
     * Creates a tween that will cycle back and forth the specified delegate tween.
     * When reaching the end, the tween will play backwards from the end until it
     * reaches the start.
     *
     * @param delegate the desired tween
     * @return a new instance
     */
    public static Tween cycle(Tween delegate) {
        return sequence(delegate, invert(delegate));
    }

    private static interface CurveFunction {
        public double curve(double input);
    }

    /**
     * Curve function for Hermite interpolation ala GLSL smoothstep().
     */
    private static class SmoothStep implements CurveFunction {

        @Override
        public double curve(double t) {
            if (t < 0) {
                return 0;
            } else if (t > 1) {
                return 1;
            }
            return t * t * (3 - 2 * t);
        }
    }

    private static class Sine implements CurveFunction {

        @Override
        public double curve(double t) {
            if (t < 0) {
                return 0;
            } else if (t > 1) {
                return 1;
            }
            // Sine starting at -90 will go from -1 to 1 through 0
            double result = Math.sin(t * Math.PI - Math.PI * 0.5);
            return (result + 1) * 0.5;
        }
    }

    private static class Curve implements Tween {
        private final Tween delegate;
        private final CurveFunction func;
        private final double length;

        public Curve(Tween delegate, CurveFunction func) {
            this.delegate = delegate;
            this.func = func;
            this.length = delegate.getLength();
        }

        @Override
        public double getLength() {
            return length;
        }

        @Override
        public boolean interpolate(double t) {
            // Sanity check the inputs
            if (t < 0) {
                return true;
            }

            if (length == 0) {
                // Caller did something strange but we'll allow it
                return delegate.interpolate(t);
            }

            t = func.curve(t / length);
            return delegate.interpolate(t * length);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[delegate=" + delegate + ", func=" + func + "]";
        }
    }

    private static class Sequence implements Tween, ContainsTweens {
        private final Tween[] delegates;
        private int current = 0;
        private double baseTime;
        private double length;

        public Sequence(Tween... delegates) {
            this.delegates = delegates;
            for (Tween t : delegates) {
                length += t.getLength();
            }
        }

        @Override
        public double getLength() {
            return length;
        }

        @Override
        public boolean interpolate(double t) {

            // Sanity check the inputs
            if (t < 0) {
                return true;
            }

            if (t < baseTime) {
                // We've rolled back before the current sequence step
                // which means we need to reset and start forward
                // again.  We have no idea how to 'roll back' and
                // this is the only way to maintain consistency.
                // The only 'normal' case where this happens is when looping
                // in which case a full rollback is appropriate.
                current = 0;
                baseTime = 0;
            }

            if (current >= delegates.length) {
                return false;
            }

            // Skip any that are done
            while (!delegates[current].interpolate(t - baseTime)) {
                // Time to go to the next one
                baseTime += delegates[current].getLength();
                current++;
                if (current >= delegates.length) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[delegates=" + Arrays.asList(delegates) + "]";
        }

        @Override
        public Tween[] getTweens() {
            return delegates;
        }
    }

    private static class Parallel implements Tween, ContainsTweens {
        private final Tween[] delegates;
        private final boolean[] done;
        private double length;
        private double lastTime;

        public Parallel(Tween... delegates) {
            this.delegates = delegates;
            done = new boolean[delegates.length];

            for (Tween t : delegates) {
                if (t.getLength() > length) {
                    length = t.getLength();
                }
            }
        }

        @Override
        public double getLength() {
            return length;
        }

        protected void reset() {
            for (int i = 0; i < done.length; i++) {
                done[i] = false;
            }
        }

        @Override
        public boolean interpolate(double t) {
            // Sanity check the inputs
            if (t < 0) {
                return true;
            }

            if (t < lastTime) {
                // We've rolled back before the last time we were given.
                // This means we may have 'done'ed a few tasks that now
                // need to be run again.  Better to just reset and start
                // over.  As mentioned in the Sequence task, the only 'normal'
                // use-case for time rolling backwards is when looping.  And
                // in that case, we want to start from the beginning anyway.
                reset();
            }
            lastTime = t;

            int runningCount = delegates.length;
            for (int i = 0; i < delegates.length; i++) {
                if (!done[i]) {
                    done[i] = !delegates[i].interpolate(t);
                }
                if (done[i]) {
                    runningCount--;
                }
            }
            return runningCount > 0;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[delegates=" + Arrays.asList(delegates) + "]";
        }

        @Override
        public Tween[] getTweens() {
            return delegates;
        }
    }

    private static class Delay extends AbstractTween {

        public Delay(double length) {
            super(length);
        }

        @Override
        protected void doInterpolate(double t) {
        }
    }

    private static class Stretch implements Tween, ContainsTweens {

        private final Tween[] delegate = new Tween[1];
        private final double length;
        private final double scale;

        public Stretch(Tween delegate, double length) {
            this.delegate[0] = delegate;

            this.length = length;

            // Caller desires delegate to be 'length' instead of
            // its actual length, so we will calculate a time scale.
            // If the desired length is longer than delegate's then
            // we need to feed time in slower, ie: scale < 1
            if (length != 0) {
                this.scale = delegate.getLength() / length;
            } else {
                this.scale = 0;
            }
        }

        @Override
        public double getLength() {
            return length;
        }

        @Override
        public Tween[] getTweens() {
            return delegate;
        }

        @Override
        public boolean interpolate(double t) {
            if (t < 0) {
                return true;
            }
            if (length > 0) {
                t *= scale;
            } else {
                t = length;
            }
            return delegate[0].interpolate(t);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[delegate=" + delegate[0] + ", length=" + length + "]";
        }
    }



    private static class Loop implements Tween, ContainsTweens {

        private final Tween[] delegate = new Tween[1];
        private final double length;
        private final int loopCount;
        private double baseTime;
        private int current = 0;

        public Loop (Tween delegate, double duration) {
            if (delegate.getLength() <= 0) {
                throw new IllegalArgumentException("Delegate length must be greater than 0");
            }
            if (duration <= 0) {
                throw new IllegalArgumentException("Duration must be greater than 0");
            }

            this.delegate[0] = delegate;
            this.length = duration;
            this.loopCount = (int) Math.ceil(duration / delegate.getLength());
        }

        public Loop (Tween delegate, int count) {
            if (count <= 0) {
                throw new IllegalArgumentException("Loop count must be greater than 0");
            }

            this.delegate[0] = delegate;
            this.length = count * delegate.getLength();
            this.loopCount = count;
        }

        @Override
        public double getLength() {
            return length;
        }

        @Override
        public Tween[] getTweens() {
            return delegate;
        }

        @Override
        public boolean interpolate(double t) {

            // Sanity check the inputs
            if (t < 0) {
                return true;
            }

            if (t < baseTime) {
                // We've rolled back before the current loop step
                // which means we need to reset and start forward
                // again.  We have no idea how to 'roll back' and
                // this is the only way to maintain consistency.
                // The only 'normal' case where this happens is when looping
                // in which case a full rollback is appropriate.
                current = 0;
                baseTime = 0;
            }

            if (current >= loopCount) {
                return false;
            }

            // Skip any that are done
            while (!delegate[0].interpolate(t - baseTime)) {
                // Time to go to the next loop
                baseTime += delegate[0].getLength();
                current++;
                if (current >= loopCount) {
                    return false;
                }
            }

            return t < length;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[delegate=" + delegate[0] + ", length=" + length + "]";
        }
    }

    private static class Invert extends AbstractTween implements ContainsTweens {

        private final Tween[] delegate = new Tween[1];

        public Invert( Tween delegate ) {
            super(delegate.getLength());
            this.delegate[0] = delegate;
        }

        @Override
        protected void doInterpolate(double t) {
            delegate[0].interpolate((1.0 - t) * getLength());
        }

        @Override
        public Tween[] getTweens() {
            return delegate;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[delegate=" + delegate[0] + ", length=" + getLength() + "]";
        }
    }
}
