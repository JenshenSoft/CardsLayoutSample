package ua.jenshensoft.cardslayout.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
    private View view;
    @NonNull
    private List<AnimationParams> objectAnimations;
    @Nullable
    private List<Animator> animators;
    //animation params
    private Interpolator interpolator;
    private int duration = 1000;
    private AnimatorSet animatorSet;

    private AwesomeAnimation(Builder builder) {
        view = builder.view;
        objectAnimations = builder.objectAnimations;
        animators = builder.animators;
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

        if (!objectAnimations.isEmpty())
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
        ObjectAnimator animator = null;
        if (params.attr != null) {
            if (params.paramsFloat != null) {
                animator = ObjectAnimator.ofFloat(view, params.attr, params.paramsFloat);
            } else if (params.paramsInt != null) {
                animator = ObjectAnimator.ofInt(view, params.attr, params.paramsInt);
            }
        } else {
            if (params.paramsFloat != null && params.propertyFloat != null) {
                animator = ObjectAnimator.ofFloat(view, params.propertyFloat, params.paramsFloat);
            } else if (params.paramsInt != null && params.propertyInt != null) {
                animator = ObjectAnimator.ofInt(view, params.propertyInt, params.paramsInt);
            }
        }
        if (animator == null) {
            throw new RuntimeException("Can't support this animation params");
        }
        if (params.evaluator != null) {
            animator.setEvaluator(params.evaluator);
        }
        if (params.interpolator != null) {
            animator.setInterpolator(params.interpolator);
        }
        return animator;
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

        @NonNull
        private View view;
        @NonNull
        private List<AnimationParams> objectAnimations;
        @Nullable
        private List<Animator> animators;
        @Nullable
        private Interpolator interpolator;
        private int duration = 1000;

        public Builder(@NonNull View view) {
            this.view = view;
            objectAnimations = new ArrayList<>();
        }

        public Builder setX(@CoordinationMode String mode, float... x) {
            if (mode.equals(COORDINATES)) {
                objectAnimations.add(new AnimationParams.Builder(View.X, x).build());
            } else if (mode.equals(TRANSITION)) {
                addTranslation(x, view.getTranslationX());
                x = deleteZeroFromArray(x);
                objectAnimations.add(new AnimationParams.Builder(View.TRANSLATION_X, x).build());
            } else {
                throw new RuntimeException("Can't support this mode");
            }
            return this;
        }

        public Builder setY(@CoordinationMode String mode, float... y) {
            if (mode.equals(COORDINATES)) {
                objectAnimations.add(new AnimationParams.Builder(View.Y, y).build());
            } else if (mode.equals(TRANSITION)) {
                addTranslation(y, view.getTranslationY());
                y = deleteZeroFromArray(y);
                objectAnimations.add(new AnimationParams.Builder(View.TRANSLATION_Y, y).build());
            } else {
                throw new RuntimeException("Can't support this mode");
            }
            return this;
        }

        public Builder setSizeX(@SizeMode String mode, float... x) {
            if (mode.equals(SCALE)) {
                objectAnimations.add(new AnimationParams.Builder(View.SCALE_X, x).build());
            } else if (mode.equals(SIZE)) {
                objectAnimations.add(new AnimationParams.Builder(PROPERTY_WIDTH, x).build());
            } else {
                throw new RuntimeException("Can't support this mode");
            }
            return this;
        }

        public Builder setSizeY(@SizeMode String mode, float... y) {
            if (mode.equals(SCALE)) {
                objectAnimations.add(new AnimationParams.Builder(View.SCALE_Y, y).build());
            } else if (mode.equals(SIZE)) {
                objectAnimations.add(new AnimationParams.Builder(PROPERTY_HEIGHT, y).build());
            } else {
                throw new RuntimeException("Can't support this mode");
            }
            return this;
        }

        public Builder setRotation(float... rotation) {
            objectAnimations.add(new AnimationParams.Builder(View.ROTATION, rotation).build());
            return this;
        }

        public Builder setAlpha(float... alpha) {
            objectAnimations.add(new AnimationParams.Builder(View.ALPHA, alpha).build());
            return this;
        }

        public Builder addObjectAnimation(AnimationParams animationParams) {
            objectAnimations.add(animationParams);
            return this;
        }

        public Builder addObjectAnimation(String attr, float... params) {
            objectAnimations.add(new AnimationParams.Builder(attr, params).build());
            return this;
        }

        public Builder addObjectAnimation(String attr, int... params) {
            objectAnimations.add(new AnimationParams.Builder(attr, params).build());
            return this;
        }

        public Builder addAnimator(@NonNull Animator animator) {
            if (animators == null) {
                animators = new ArrayList<>();
            }
            animators.add(animator);
            return this;
        }

        public Builder setInterpolator(@NonNull Interpolator interpolator) {
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

    public static class AnimationParams {
        public String attr;
        @Nullable
        private Property<View, Float> propertyFloat;
        @Nullable
        private Property<View, Integer> propertyInt;
        @Nullable
        private Interpolator interpolator;
        @Nullable
        private TypeEvaluator evaluator;
        @Nullable
        private float[] paramsFloat;
        @Nullable
        private int[] paramsInt;

        private AnimationParams(Builder builder) {
            this.attr = builder.attr;
            this.propertyFloat = builder.propertyFloat;
            this.propertyInt = builder.propertyInt;
            this.interpolator = builder.interpolator;
            this.evaluator = builder.evaluator;
            this.paramsFloat = builder.paramsFloat;
            this.paramsInt = builder.paramsInt;
        }

        public static class Builder {
            public String attr;
            @Nullable
            private Property<View, Float> propertyFloat;
            @Nullable
            private Property<View, Integer> propertyInt;
            @Nullable
            private float[] paramsFloat;
            @Nullable
            private int[] paramsInt;

            //optional
            @Nullable
            private Interpolator interpolator;
            @Nullable
            private TypeEvaluator evaluator;

            public Builder(String attr, @NonNull float... params) {
                this.attr = attr;
                this.paramsFloat = params;
            }

            public Builder(String attr, @NonNull int... params) {
                this.attr = attr;
                this.paramsInt = params;
            }

            public Builder(@NonNull Property<View, Float> property, @NonNull float... params) {
                this.propertyFloat = property;
                this.paramsFloat = params;
            }

            public Builder(@NonNull Property<View, Integer> property, @NonNull int... params) {
                this.propertyInt = property;
                this.paramsInt = params;
            }

            public Builder setInterpolator(@NonNull Interpolator interpolator) {
                this.interpolator = interpolator;
                return this;
            }

            public Builder setEvaluator(@NonNull TypeEvaluator evaluator) {
                this.evaluator = evaluator;
                return this;
            }

            public AnimationParams build() {
                return new AnimationParams(this);
            }
        }
    }
}
