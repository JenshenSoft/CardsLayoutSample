package ua.jenshensoft.cardslayout.util;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.annotation.StringDef;
import android.util.Property;
import android.view.View;
import android.view.animation.Interpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import static ua.jenshensoft.cardslayout.util.AwesomeAnimation.CoordinationMode.COORDINATES;
import static ua.jenshensoft.cardslayout.util.AwesomeAnimation.CoordinationMode.TRANSITION;
import static ua.jenshensoft.cardslayout.util.AwesomeAnimation.SizeMode.SCALE;
import static ua.jenshensoft.cardslayout.util.AwesomeAnimation.SizeMode.SIZE;


public class AwesomeAnimation {

    private static final Property<View, Float> PROPERTY_WIDTH =
            new Property<View, Float>(Float.class, "viewLayoutWidth") {

                @Override
                public void set(View object, Float value) {
                    object.getLayoutParams().width = value.intValue();
                    object.requestLayout();
                }

                @Override
                public Float get(View object) {
                    return (float) object.getLayoutParams().width;
                }
            };
    private static final Property<View, Float> PROPERTY_HEIGHT =
            new Property<View, Float>(Float.class, "viewLayoutHeight") {

                @Override
                public void set(View object, Float value) {
                    object.getLayoutParams().height = value.intValue();
                    object.requestLayout();
                }

                @Override
                public Float get(View object) {
                    return (float) object.getLayoutParams().height;
                }
            };
    private AnimationParams x;
    private AnimationParams y;
    private AnimationParams sizeX;
    private AnimationParams sizeY;
    private float[] rotation;
    private float[] alpha;
    private View view;
    private List<AnimationParams> objectAnimations;
    private List<Animator> animators;
    //animation params
    private Interpolator interpolator;
    private int duration = 1000;
    private AnimatorSet animatorSet;

    private AwesomeAnimation(Builder builder) {
        view = builder.view;
        objectAnimations = builder.objectAnimations;
        animators = builder.animators;
        x = builder.x;
        y = builder.y;
        sizeX = builder.sizeX;
        sizeY = builder.sizeY;
        rotation = builder.rotation;
        alpha = builder.alpha;
        interpolator = builder.interpolator;
        duration = builder.duration;
        animatorSet = createAnimationSet();
    }

    public void start() {
        animatorSet.start();
    }

    public AnimatorSet getAnimatorSet() {
        return animatorSet;
    }

    private AnimatorSet createAnimationSet() {
        List<Animator> animators = new ArrayList<>();
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        if (x != null && x.params != null) {
            animators.add(createAnimation(view, x));
        }

        if (y != null && y.params != null) {
            animators.add(createAnimation(view, y));
        }

        if (sizeX != null && sizeX.params != null) {
            animators.add(createAnimation(view, sizeX));
        }

        if (sizeY != null && sizeY.params != null) {
            animators.add(createAnimation(view, sizeY));
        }

        if (rotation != null) {
            animators.add(ObjectAnimator.ofFloat(view, "rotation", rotation));
        }

        if (alpha != null) {
            animators.add(ObjectAnimator.ofFloat(view, "alpha", alpha));
        }

        if (objectAnimations != null)
            for (AnimationParams customAnimation : objectAnimations) {
                animators.add(createAnimation(view, customAnimation));
            }

        if (this.animators != null)
            for (Animator animator : this.animators) {
                animators.add(animator);
            }
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animators);
        if (interpolator != null) {
            animatorSet.setInterpolator(interpolator);
        }
        animatorSet.setDuration(duration);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                view.setLayerType(View.LAYER_TYPE_NONE, null);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setLayerType(View.LAYER_TYPE_NONE, null);
            }
        });
        return animatorSet;
    }

    private ObjectAnimator createAnimation(View view, AnimationParams params) {
        if (params.attr != null) {
            return ObjectAnimator.ofFloat(view, params.attr, params.params);
        } else {
            return ObjectAnimator.ofFloat(view, params.property, params.params);
        }
    }

    @StringDef({SCALE, SIZE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SizeMode {
        String SCALE = "scale";
        String SIZE = "size";
    }

    @StringDef({TRANSITION, COORDINATES})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CoordinationMode {
        String TRANSITION = "transition";
        String COORDINATES = "coordinates";
    }

    public static class Builder {

        private View view;
        private List<AnimationParams> objectAnimations;
        private List<Animator> animators;
        private AnimationParams x;
        private AnimationParams y;
        private AnimationParams sizeX;
        private AnimationParams sizeY;
        private float[] rotation;
        private float[] alpha;
        private Interpolator interpolator;
        private int duration = 1000;

        public Builder(View view) {
            this.view = view;
        }

        public Builder setX(@CoordinationMode String mode, float... x) {
            if (mode.equals(COORDINATES)) {
                this.x = new AnimationParams(View.X, x);
            } else if (mode.equals(TRANSITION)) {
                addTranslation(x, view.getTranslationX());
                x = deleteZeroFromArray(x);
                this.x = new AnimationParams(View.TRANSLATION_X, x);
            } else {
                throw new RuntimeException("Can't support this mode");
            }
            return this;
        }

        public Builder setY(@CoordinationMode String mode, float... y) {
            if (mode.equals(COORDINATES)) {
                this.y = new AnimationParams(View.Y, y);
            } else if (mode.equals(TRANSITION)) {
                addTranslation(y, view.getTranslationY());
                y = deleteZeroFromArray(y);
                this.y = new AnimationParams(View.TRANSLATION_Y, y);
            } else {
                throw new RuntimeException("Can't support this mode");
            }
            return this;
        }

        public Builder setSizeX(@SizeMode String mode, float... x) {
            if (mode.equals(SCALE)) {
                this.sizeX = new AnimationParams(View.SCALE_X, x);
            } else if (mode.equals(SIZE)) {
                this.sizeX = new AnimationParams(PROPERTY_WIDTH, x);
            } else {
                throw new RuntimeException("Can't support this mode");
            }
            return this;
        }

        public Builder setSizeY(@SizeMode String mode, float... y) {
            if (mode.equals(SCALE)) {
                this.sizeY = new AnimationParams(View.SCALE_Y, y);
            } else if (mode.equals(SIZE)) {
                this.sizeY = new AnimationParams(PROPERTY_HEIGHT, y);
            } else {
                throw new RuntimeException("Can't support this mode");
            }
            return this;
        }

        public Builder setRotation(float... rotation) {
            this.rotation = rotation;
            return this;
        }

        public Builder setAlpha(float... alpha) {
            this.alpha = alpha;
            return this;
        }

        public Builder addObjectAnimation(String attr, float... params) {
            if (objectAnimations == null) {
                objectAnimations = new ArrayList<>();
            }
            objectAnimations.add(new AnimationParams(attr, params));
            return this;
        }

        public Builder addAnimator(Animator animator) {
            if (animators == null) {
                animators = new ArrayList<>();
            }
            animators.add(animator);
            return this;
        }

        public Builder setInterpolator(Interpolator interpolator) {
            this.interpolator = interpolator;
            return this;
        }

        public Builder setDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public AwesomeAnimation build() {
            return new AwesomeAnimation(this);
        }

        private float[] deleteZeroFromArray(float[] array) {
            if (array == null) {
                return null;
            }
            List<Float> list = new ArrayList<>();
            for (float value : array) {
                list.add(value);
            }
            list.remove(0.0f);
            if (list.isEmpty()) {
                return null;
            } else {
                array = new float[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    array[i] = list.get(i);
                }
                return array;
            }
        }

        private void addTranslation(float[] array, float translation) {
            if (array == null) {
                return;
            }
            for (int i = 0; i < array.length; i++) {
                array[i] += translation;
            }
        }
    }

    private static class AnimationParams {
        public String attr;
        private Property<View, Float> property;
        private float[] params;

        AnimationParams(String attr, float[] params) {
            this.attr = attr;
            this.params = params;
        }

        AnimationParams(Property<View, Float> property, float[] params) {
            this.property = property;
            this.params = params;
        }
    }
}
