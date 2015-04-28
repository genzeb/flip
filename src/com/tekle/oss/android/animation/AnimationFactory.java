/**
 * Copyright (c) 2012 Ephraim Tekle genzeb@gmail.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and 
 * associated documentation files (the "Software"), to deal in the Software without restriction, including 
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the 
 * following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial 
 * portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN 
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 *  @author Ephraim A. Tekle
 *
 */
package com.tekle.oss.android.animation;  

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ViewAnimator;

/**
 * This class contains methods for creating {@link Animation} objects for some of the most common animation, including a 3D flip animation, {@link FlipAnimation}.
 * Furthermore, utility methods are provided for initiating fade-in-then-out and flip animations.
 * 
 * @author Ephraim A. Tekle
 *
 */
public class AnimationFactory {
	
	/** In some devices the flip animation runs very slowly. In these cases we change to fade animation. 
	 * 	(The format of each device string is "MANUFACTURER,MODEL")
	 * */
	public static String[] SLOW_FLIP_DEVICES = new String[]{"LGE,g3","LGE,Nexus 5"};
	
	private static ValueAnimator mFlipAnimator = null;

	
	private static final int DEFAULT_FLIP_TRANSITION_DURATION = 500;

	/**
	 * The {@code FlipDirection} enumeration defines the most typical flip view transitions: left-to-right and right-to-left. {@code FlipDirection} is used during the creation of {@link FlipAnimation} animations.
	 * 
	 * @author Ephraim A. Tekle
	 *
	 */
	public static enum FlipDirection {
        LEFT_RIGHT,
        RIGHT_LEFT,
        TOP_BOTTOM,
        BOTTOM_TOP;
		
		public float getStartDegreeForFirstView() {
			return 0;
		}
		
		public float getStartDegreeForSecondView() {
			switch(this) {
            case LEFT_RIGHT:
            case TOP_BOTTOM:
                return -90;
            case RIGHT_LEFT:
            case BOTTOM_TOP:
                return 90;
            default:
                return 0;
			}
		}
		
		public float getEndDegreeForFirstView() {
			switch(this) {
            case LEFT_RIGHT:
            case TOP_BOTTOM:
                return 90;
            case RIGHT_LEFT:
            case BOTTOM_TOP:
                return -90;
            default:
                return 0;
			}
		}
		
		public float getEndDegreeForSecondView() {
			return 0;
		}
		
		public FlipDirection theOtherDirection() {
			switch(this) {
            case LEFT_RIGHT:
                return RIGHT_LEFT;
            case TOP_BOTTOM:
                return BOTTOM_TOP;
            case RIGHT_LEFT:
                return LEFT_RIGHT;
            case BOTTOM_TOP:
                return TOP_BOTTOM;
            default:
                return null;
			}
		}
	};
	 
	
	/**
	 * Create a pair of {@link FlipAnimation} that can be used to flip 3D transition from {@code fromView} to {@code toView}. A typical use case is with {@link ViewAnimator} as an out and in transition.
	 * 
	 * NOTE: Avoid using this method. Instead, use {@link #flipTransition}.
	 *  
	 * @param fromView the view transition away from
	 * @param toView the view transition to
	 * @param dir the flip direction
	 * @param duration the transition duration in milliseconds
	 * @param interpolator the interpolator to use (pass {@code null} to use the {@link AccelerateInterpolator} interpolator) 
	 * @return
	 */
	public static Animation[] flipAnimation(final View fromView, final View toView, FlipDirection dir, long duration, Interpolator interpolator) {
		
		Animation[] result = new Animation[2];
		float centerX;
		float centerY;
		
		centerX = fromView.getWidth() / 2.0f;
		centerY = fromView.getHeight() / 2.0f;

        FlipAnimation outFlip= new FlipAnimation(dir.getStartDegreeForFirstView(), dir.getEndDegreeForFirstView(), centerX, centerY, FlipAnimation.SCALE_DEFAULT, FlipAnimation.ScaleUpDownEnum.SCALE_DOWN);
		outFlip.setDuration(duration);
		outFlip.setFillAfter(true);
		outFlip.setInterpolator(interpolator==null?new AccelerateInterpolator():interpolator);

        if (dir == FlipDirection.BOTTOM_TOP || dir == FlipDirection.TOP_BOTTOM)
            outFlip.setDirection(FlipAnimation.ROTATION_X);
        else
            outFlip.setDirection(FlipAnimation.ROTATION_Y);

		AnimationSet outAnimation = new AnimationSet(true);
		outAnimation.addAnimation(outFlip); 
		result[0] = outAnimation; 
		
		// Uncomment the following if toView has its layout established (not the case if using ViewFlipper and on first show)
		//centerX = toView.getWidth() / 2.0f;
		//centerY = toView.getHeight() / 2.0f; 

        FlipAnimation inFlip = new FlipAnimation(dir.getStartDegreeForSecondView(), dir.getEndDegreeForSecondView(), centerX, centerY, FlipAnimation.SCALE_DEFAULT, FlipAnimation.ScaleUpDownEnum.SCALE_UP);
		inFlip.setDuration(duration);
		inFlip.setFillAfter(true);
		inFlip.setInterpolator(interpolator == null ? new AccelerateInterpolator() : interpolator);
		inFlip.setStartOffset(duration);

        if (dir == FlipDirection.BOTTOM_TOP || dir == FlipDirection.TOP_BOTTOM)
            inFlip.setDirection(FlipAnimation.ROTATION_X);
        else
            inFlip.setDirection(FlipAnimation.ROTATION_Y);

		AnimationSet inAnimation = new AnimationSet(true); 
		inAnimation.addAnimation(inFlip); 
		result[1] = inAnimation; 
		
		return result;
		
	}

	/**
	 * Flip to the next view of the {@code ViewAnimator}'s subviews. A call to this method will initiate a {@link FlipAnimation} to show the next View.  
	 * If the currently visible view is the last view, flip direction will be reversed for this transition.
	 *  
	 * @param viewAnimator the {@code ViewAnimator}
	 * @param dir the direction of flip
	 */
	public static void flipTransition(final ViewAnimator viewAnimator, FlipDirection dir) {   
		flipTransition(viewAnimator, dir, DEFAULT_FLIP_TRANSITION_DURATION);
	}
	
	/**
	 * Flip to the next view of the {@code ViewAnimator}'s subviews. A call to this method will initiate 
	 * a {@link FlipAnimation} to show the next View. If the currently visible view is the last view, 
	 * flip direction will be reversed for this transition.
	 * 
	 * In some devices, 2K screen devices and some others with Lollipop, the flip 
	 * animation runs slowly or laggy. To avoid this, device hardware information
	 * is checked against a list {@link SLOW_FLIP_DEVICES}.
	 *  
	 * @param viewAnimator
	 * @param dir
	 * @param checkForLaggyDevices	If set to TRUE, flip effect will occur only
	 * 															if the device is not a detected laggy device with
	 * 															Flip effect.
	 */
	public static void flipTransition(final ViewAnimator viewAnimator, FlipDirection dir, boolean checkForLaggyDevices, boolean enableFadeForLaggyDevices) {
		if(checkForLaggyDevices && isFlipAnimationSlow(device_getExtraInfo()) ){
			//In this devices the flip transition runs slowly so we change to fade animation
			if(enableFadeForLaggyDevices)				
				fadeTransition(viewAnimator, 50, 50); //Default fade duration.
			else
				viewAnimator.showNext();
		}else{
			flipTransition(viewAnimator, dir, DEFAULT_FLIP_TRANSITION_DURATION);
		}
	}

	/**
	 * Flip to the next view of the {@code ViewAnimator}'s subviews. A call 
	 * to this method will initiate a {@link FlipAnimation} to show the next 
	 * View. If the currently visible view is the last view, flip direction 
	 * will be reversed for this transition.
	 *  
	 * @param viewAnimator the {@code ViewAnimator}
	 * @param dir the direction of flip
	 * @param duration the transition duration in milliseconds
	 */
	public static void flipTransition(final ViewAnimator viewAnimator, FlipDirection dir, long duration) {   
		
		final View fromView = viewAnimator.getCurrentView();
		final int currentIndex = viewAnimator.getDisplayedChild();
		final int nextIndex = (currentIndex + 1)%viewAnimator.getChildCount();
		
		final View toView = viewAnimator.getChildAt(nextIndex);		
				
		if(android.os.Build.VERSION.SDK_INT>=12) {
			//New way of flipping.
			flipTransition(fromView, toView);
		}else{
			//Traditional flip.
			Animation[] animc = AnimationFactory.flipAnimation(fromView, toView, 
					(nextIndex < currentIndex?dir.theOtherDirection():dir), duration, null);
			viewAnimator.setOutAnimation(animc[0]);
			viewAnimator.setInAnimation(animc[1]);
			viewAnimator.showNext();
		}	   
	}
	
	/**
	 * Flip to the next view of the {@code ViewAnimator}'s subviews. A call 
	 * to this method will initiate a {@link FlipAnimation} to show the next 
	 * View. If the currently visible view is the last view, flip direction 
	 * will be reversed for this transition.
	 * 
	 * In some devices, 2K screen devices and some others with Lollipop, the flip 
	 * animation runs slowly or laggy. To avoid this, device hardware information
	 * is checked against a list {@link SLOW_FLIP_DEVICES}.
	 * 
	 * @param viewAnimator
	 * @param dir
	 * @param duration
	 * @param checkForLaggyDevices	If set to TRUE, flip effect will occur only
	 * 															if the device is not a detected laggy device with
	 * 															Flip effect.
	 */
	public static void flipTransition(final ViewAnimator viewAnimator, FlipDirection dir, long duration, boolean checkForLaggyDevices, boolean enableFadeForLaggyDevices) {   
		
		final View fromView = viewAnimator.getCurrentView();
		final int currentIndex = viewAnimator.getDisplayedChild();
		final int nextIndex = (currentIndex + 1)%viewAnimator.getChildCount();
		
		final View toView = viewAnimator.getChildAt(nextIndex);
		
		if(checkForLaggyDevices && isFlipAnimationSlow(device_getExtraInfo()) ){
			//In this devices the flip transition runs slowly so we change to fade animation
			if(enableFadeForLaggyDevices)
				fadeTransition(viewAnimator, 50, 50); //Default fade duration.
			else
				viewAnimator.showNext();
		}else{		
			if(android.os.Build.VERSION.SDK_INT>=12) {
				//New way of flipping.
				flipTransition(fromView, toView); 
			}else{
				//Traditional flip.
				Animation[] animc = AnimationFactory.flipAnimation(fromView, toView, 
						(nextIndex < currentIndex?dir.theOtherDirection():dir), duration, null);
				viewAnimator.setOutAnimation(animc[0]);
				viewAnimator.setInAnimation(animc[1]);
				viewAnimator.showNext();
			}
		}		   
	}
	
	//////////////
	
	
	/**
	 * Get device extra information like Manufacturer,
	 * brand and device model info.
	 * 
	 * @return
	 */
	private static String device_getExtraInfo() {
		String extra = 	android.os.Build.MANUFACTURER + 
						(android.os.Build.BRAND!=null?"/" + android.os.Build.BRAND:"") +
						"/" + android.os.Build.DEVICE + 
						"/" + android.os.Build.MODEL +
						"/" + android.os.Build.HARDWARE +
						"/" + android.os.Build.PRODUCT;
						
		return extra;
	}
	
	/**
	 * This method makes a flip by using ValueAnimator and
	 * a Flip Listener.<br><br>
	 * 
	 * NOTE: Only for Android API Level 12+
	 * 
	 * @param fromView
	 * @param toView
	 */
	@SuppressLint("NewApi")
	private static void flipTransition(View fromView, View toView) {
		if(mFlipAnimator==null) {
			mFlipAnimator = ValueAnimator.ofFloat(0f, 1f);
		}
		
		mFlipAnimator.addUpdateListener(new FlipListener(fromView, toView));
		if(mFlipAnimator.getAnimatedFraction() == 1) {
			//Already flipped
			mFlipAnimator.reverse();
		}else{
			mFlipAnimator.start();
		}
		
		/*
		final float currentValue = mFlipAnimator.getAnimatedFraction();
    boolean isFlipping = (currentValue < 1 && currentValue > 0);*/
	}
	
	/**
	 * Fades out the current view to the next view of the {@code ViewAnimator}'s subviews with a fade in
	 * effect. If the currently visible view is the last view, fade will restore the front view.
	 * 
	 * @param viewAnimator
	 * @param fadeOutDuration
	 * @param fadeInDuration
	 */
	public static void fadeTransition(final ViewAnimator viewAnimator, long fadeOutDuration, long fadeInDuration) {

		final View fromView = viewAnimator.getCurrentView();
		final int currentIndex = viewAnimator.getDisplayedChild();
		final int nextIndex = (currentIndex + 1) % viewAnimator.getChildCount();

		final View toView = viewAnimator.getChildAt(nextIndex);
		
		Animation[] animc = fadeTransition(fromView, toView, fadeOutDuration, fadeInDuration);
		
		viewAnimator.setOutAnimation(animc[0]);
		viewAnimator.setInAnimation(animc[1]);
		viewAnimator.showNext();
		
	}
	
	/**
	 * Fades out the current view to the next view of the {@code ViewAnimator}'s subviews with a fade in
	 * effect. If the currently visible view is the last view, fade will restore the front view.
	 * 
	 * @param fromView
	 * @param toView
	 * @param fadeOutDuration
	 * @param fadeInDuration
	 * @return
	 */
	public static Animation[] fadeTransition(final View fromView, final View toView, long fadeOutDuration, long fadeInDuration) {

		Animation[] result = new Animation[2];
		result[0] = AnimationFactory.fadeOutAnimation(fadeOutDuration, fromView);
		result[1] = AnimationFactory.fadeInAnimation(fadeInDuration, toView);
		
		return result;
	}
	
	private static boolean isFlipAnimationSlow(String deviceHardwareInfo) {
		boolean res = false;
		
		String[] dInfo = null;
		String dManufacturer = null, dModel = null;
		for(String d:SLOW_FLIP_DEVICES) {
			dInfo = d.split(",");
			dManufacturer = dInfo[0];
			dModel = dInfo[1];
			
			if(deviceHardwareInfo.contains(dManufacturer) && deviceHardwareInfo.contains(dModel)){
				res = true;
				break;
			}
		}
		
		return res;
	}
	
	
	/**
	 * Slide animations to enter a view from left.
	 * 
	 * @param duration the animation duration in milliseconds
	 * @param interpolator the interpolator to use (pass {@code null} to use the {@link AccelerateInterpolator} interpolator) 	
	 * @return a slide transition animation
	 */
	public static Animation inFromLeftAnimation(long duration, Interpolator interpolator) {
		Animation inFromLeft = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT,  -1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
				Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
		);
		inFromLeft.setDuration(duration);
		inFromLeft.setInterpolator(interpolator==null?new AccelerateInterpolator():interpolator); //AccelerateInterpolator
		return inFromLeft;
	}
 
	/**
	 * Slide animations to hide a view by sliding it to the right
	 * 
	 * @param duration the animation duration in milliseconds
	 * @param interpolator the interpolator to use (pass {@code null} to use the {@link AccelerateInterpolator} interpolator) 	
	 * @return a slide transition animation
	 */
	public static Animation outToRightAnimation(long duration, Interpolator interpolator) {
		Animation outtoRight = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  +1.0f,
				Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
		);
		outtoRight.setDuration(duration);
		outtoRight.setInterpolator(interpolator==null?new AccelerateInterpolator():interpolator);
		return outtoRight;
	}
 
	/**
	 * Slide animations to enter a view from right.
	 * 
	 * @param duration the animation duration in milliseconds
	 * @param interpolator the interpolator to use (pass {@code null} to use the {@link AccelerateInterpolator} interpolator) 	
	 * @return a slide transition animation
	 */
	public static Animation inFromRightAnimation(long duration, Interpolator interpolator) {

		Animation inFromRight = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT,  +1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
				Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
		);
		inFromRight.setDuration(duration);
		inFromRight.setInterpolator(interpolator==null?new AccelerateInterpolator():interpolator);
		return inFromRight;
	}
 
	/**
	 * Slide animations to hide a view by sliding it to the left.
	 * 
	 * @param duration the animation duration in milliseconds
	 * @param interpolator the interpolator to use (pass {@code null} to use the {@link AccelerateInterpolator} interpolator) 	
	 * @return a slide transition animation
	 */
	public static Animation outToLeftAnimation(long duration, Interpolator interpolator) {
		Animation outtoLeft = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  -1.0f,
				Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
		);
		outtoLeft.setDuration(duration);
		outtoLeft.setInterpolator(interpolator==null?new AccelerateInterpolator():interpolator);
		return outtoLeft;
	} 
 
	/**
	 * Slide animations to enter a view from top.
	 * 
	 * @param duration the animation duration in milliseconds
	 * @param interpolator the interpolator to use (pass {@code null} to use the {@link AccelerateInterpolator} interpolator) 	
	 * @return a slide transition animation
	 */
	public static Animation inFromTopAnimation(long duration, Interpolator interpolator) {
		Animation infromtop = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f
		);
		infromtop.setDuration(duration);
		infromtop.setInterpolator(interpolator==null?new AccelerateInterpolator():interpolator);
		return infromtop;
	} 
 
	/**
	 * Slide animations to hide a view by sliding it to the top
	 * 
	 * @param duration the animation duration in milliseconds
	 * @param interpolator the interpolator to use (pass {@code null} to use the {@link AccelerateInterpolator} interpolator) 	
	 * @return a slide transition animation
	 */
	public static Animation outToTopAnimation(long duration, Interpolator interpolator) {
		Animation outtotop = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
				Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT, -1.0f
		);
		outtotop.setDuration(duration); 
		outtotop.setInterpolator(interpolator==null?new AccelerateInterpolator():interpolator); 
		return outtotop;
	} 

	/**
	 * A fade animation that will fade the subject in by changing alpha from 0 to 1.
	 * 
	 * @param duration the animation duration in milliseconds
	 * @param delay how long to wait before starting the animation, in milliseconds
	 * @return a fade animation
	 * @see #fadeInAnimation(View, long)
	 */
	public static Animation fadeInAnimation(long duration, long delay) {  
		
		Animation fadeIn = new AlphaAnimation(0, 1);
		fadeIn.setInterpolator(new DecelerateInterpolator());  
		fadeIn.setDuration(duration);
		fadeIn.setStartOffset(delay);
		
		return fadeIn;
	}

	/**
	 * A fade animation that will fade the subject out by changing alpha from 1 to 0.
	 * 
	 * @param duration the animation duration in milliseconds
	 * @param delay how long to wait before starting the animation, in milliseconds
	 * @return a fade animation
	 * @see #fadeOutAnimation(View, long)
	 */
	public static Animation fadeOutAnimation(long duration, long delay) {   

		Animation fadeOut = new AlphaAnimation(1, 0);
		fadeOut.setInterpolator(new AccelerateInterpolator());
		fadeOut.setStartOffset(delay);
		fadeOut.setDuration(duration);

		return fadeOut;
	} 

	/**
	 * A fade animation that will ensure the View starts and ends with the correct visibility
	 * @param view the View to be faded in
	 * @param duration the animation duration in milliseconds
	 * @return a fade animation that will set the visibility of the view at the start and end of animation
	 */
	public static Animation fadeInAnimation(long duration, final View view) { 
		Animation animation = fadeInAnimation(500, 0); 

	    animation.setAnimationListener(new AnimationListener() { 
			@Override
			public void onAnimationEnd(Animation animation) {
				view.setVisibility(View.VISIBLE);
			} 
			
			@Override
			public void onAnimationRepeat(Animation animation) { 
			}  
			
			@Override
			public void onAnimationStart(Animation animation) {
				view.setVisibility(View.GONE); 
			} 
	    });
	    
	    return animation;
	}

	/**
	 * A fade animation that will ensure the View starts and ends with the correct visibility
	 * @param view the View to be faded out
	 * @param duration the animation duration in milliseconds
	 * @return a fade animation that will set the visibility of the view at the start and end of animation
	 */
	public static Animation fadeOutAnimation(long duration, final View view) {
		
		Animation animation = fadeOutAnimation(500, 0); 

	    animation.setAnimationListener(new AnimationListener() { 
			@Override
			public void onAnimationEnd(Animation animation) {
				view.setVisibility(View.GONE);
			} 
			
			@Override
			public void onAnimationRepeat(Animation animation) { 
			}  
			
			@Override
			public void onAnimationStart(Animation animation) {
				view.setVisibility(View.VISIBLE); 
			} 
	    });
	    
	    return animation;
		
	}

	/**
	 * Creates a pair of animation that will fade in, delay, then fade out
	 * @param duration the animation duration in milliseconds
	 * @param delay how long to wait after fading in the subject and before starting the fade out
	 * @return a fade in then out animations
	 */
	public static Animation[] fadeInThenOutAnimation(long duration, long delay) {  
		return new Animation[] {fadeInAnimation(duration,0), fadeOutAnimation(duration, duration+delay)};
	}  
	
	/**
	 * Fades the view in. Animation starts right away.
	 * @param v the view to be faded in
	 */
	public static void fadeOut(View v) { 
		if (v==null) return;  
	    v.startAnimation(fadeOutAnimation(500, v)); 
	} 
	
	/**
	 * Fades the view out. Animation starts right away.
	 * @param v the view to be faded out
	 */
	public static void fadeIn(View v) { 
		if (v==null) return;
		
	    v.startAnimation(fadeInAnimation(500, v)); 
	}
	
	/**
	 * Fades the view in, delays the specified amount of time, then fades the view out
	 * @param v the view to be faded in then out
	 * @param delay how long the view will be visible for
	 */
	public static void fadeInThenOut(final View v, long delay) {
		if (v==null) return;
		 
		v.setVisibility(View.VISIBLE);
		AnimationSet animation = new AnimationSet(true);
		Animation[] fadeInOut = fadeInThenOutAnimation(500,delay); 
	    animation.addAnimation(fadeInOut[0]);
	    animation.addAnimation(fadeInOut[1]);
	    animation.setAnimationListener(new AnimationListener() { 
			@Override
			public void onAnimationEnd(Animation animation) {
				v.setVisibility(View.GONE);
			} 
			@Override
			public void onAnimationRepeat(Animation animation) { 
			}  
			@Override
			public void onAnimationStart(Animation animation) {
				v.setVisibility(View.VISIBLE); 
			} 
	    });
	    
	    v.startAnimation(animation); 
	}

}
