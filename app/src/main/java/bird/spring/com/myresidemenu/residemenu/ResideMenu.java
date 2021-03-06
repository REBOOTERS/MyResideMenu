package bird.spring.com.myresidemenu.residemenu;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;
import java.util.List;

import bird.spring.com.myresidemenu.R;

public class ResideMenu extends FrameLayout implements OnClickListener {

    public static final int DIRECTION_LEFT = 0;
    public static final int DIRECTION_RIGHT = 1;
    private static final int PRESSED_MOVE_HORIZANTAL = 2;
    private static final int PRESSED_DOWN = 3;
    private static final int PRESSED_DONE = 4;
    private static final int PRESSED_MOVE_VERTICAL = 5;

    private ImageView imageViewShadow;
    private ImageView imageViewBackground;
    private LinearLayout layoutLeftMenu;
    private LinearLayout layoutRightMenu;
    private LinearLayout layoutInfo;
    private LinearLayout layoutSetting;
    private RelativeLayout leftMenu;
    private RelativeLayout rightMenu;
    private RelativeLayout scrollViewMenu;
    /**
     * the activity that view attach to
     */
    private Activity activity;
    /**
     * the decorview of the activity
     */
    private ViewGroup viewDecor;
    /**
     * the viewgroup of the activity
     */
    private TouchDisableView viewActivity;
    /**
     * the flag of menu open status
     */
    private boolean isOpened;

    /**
     * the view which don't want to intercept touch event
     */
    private List<View> ignoredViews;
    private List<ResideMenuItem> leftMenuItems;
    private List<ResideMenuItem> rightMenuItems;
    private DisplayMetrics displayMetrics = new DisplayMetrics();
    private OnMenuListener menuListener;
    private float lastRawX;
    private boolean isInIgnoredView = false;
    private int scaleDirection = DIRECTION_LEFT;
    private int pressedState = PRESSED_DOWN;
    private List<Integer> disabledSwipeDirection = new ArrayList<Integer>();

    private TextView setting, comment;
    private SettingLayoutListener settingListener;

    private int screenWidth;
    private float translateXParam = 0.85f;
    private float MenuViewWidth;

    public ResideMenu(Context context) {
        super(context);
        initViews(context);
    }

    private void initViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.residemenu, this);
        leftMenu = (RelativeLayout) findViewById(R.id.rl_left_menu);
        rightMenu = (RelativeLayout) findViewById(R.id.rl_right_menu);
        imageViewShadow = (ImageView) findViewById(R.id.iv_shadow);
        layoutLeftMenu = (LinearLayout) findViewById(R.id.layout_left_menu);
        layoutRightMenu = (LinearLayout) findViewById(R.id.layout_right_menu);
        imageViewBackground = (ImageView) findViewById(R.id.iv_background);

        layoutInfo = (LinearLayout) findViewById(R.id.layout_info);

        layoutSetting = (LinearLayout) findViewById(R.id.layout_setting);
        setting = (TextView) findViewById(R.id.setting);
        setting.setOnClickListener(this);
        comment = (TextView) findViewById(R.id.comment);
        comment.setOnClickListener(this);


        MenuViewWidth = (1 - translateXParam) * screenWidth;


    }

    /**
     * use the method to set up the activity which residemenu need to show;
     *
     * @param activity
     */
    public void attachToActivity(Activity activity) {
        initValue(activity);
        viewDecor.addView(this, 0);
        setViewPadding();

    }

    private void initValue(Activity activity) {
        this.activity = activity;
        leftMenuItems = new ArrayList<ResideMenuItem>();
        rightMenuItems = new ArrayList<ResideMenuItem>();
        ignoredViews = new ArrayList<View>();
        viewDecor = (ViewGroup) activity.getWindow().getDecorView();
        viewActivity = new TouchDisableView(this.activity);
        View mContent = viewDecor.getChildAt(0);
        viewDecor.removeViewAt(0);
        viewActivity.setContent(mContent);
        addView(viewActivity);
        screenWidth = getScreenWidth();
    }

    /**
     * set the menu background picture;
     *
     * @param imageResrouce
     */
    public void setBackground(int imageResrouce) {
        imageViewBackground.setImageResource(imageResrouce);
    }

    /**
     * the visiblity of shadow under the activity view;
     *
     * @param isVisible
     */
    public void setShadowVisible(boolean isVisible) {
        if (isVisible)
            imageViewShadow.setImageResource(R.drawable.shadow);
        else
            imageViewShadow.setImageBitmap(null);
    }

    /**
     * 添加用户信息
     *
     * @param menuInfo
     */
    public void addMenuInfo(ResideMenuInfo menuInfo) {
        layoutInfo.addView(menuInfo);
    }

    /**
     * add a single items;
     *
     * @param menuItem
     * @param direction
     */
    public void addMenuItem(ResideMenuItem menuItem, int direction) {
        if (direction == DIRECTION_LEFT) {
            this.leftMenuItems.add(menuItem);
            layoutLeftMenu.addView(menuItem);
        } else {
            this.rightMenuItems.add(menuItem);
            layoutRightMenu.addView(menuItem);
        }
    }

    /**
     * set the menu items by array list;
     *
     * @param menuItems
     * @param direction
     */
    public void setMenuItems(List<ResideMenuItem> menuItems, int direction) {
        if (direction == DIRECTION_LEFT)
            this.leftMenuItems = menuItems;
        else
            this.rightMenuItems = menuItems;
        rebuildMenu();
    }

    private void rebuildMenu() {
        layoutLeftMenu.removeAllViews();
        layoutRightMenu.removeAllViews();
        for (int i = 0; i < leftMenuItems.size(); i++)
            layoutLeftMenu.addView(leftMenuItems.get(i), i);
        for (int i = 0; i < rightMenuItems.size(); i++)
            layoutRightMenu.addView(rightMenuItems.get(i), i);
    }

    /**
     * get the menu items;
     *
     * @return
     */
    public List<ResideMenuItem> getMenuItems(int direction) {
        if (direction == DIRECTION_LEFT)
            return leftMenuItems;
        else
            return rightMenuItems;
    }


    /**
     * if you need to do something on the action of closing or opening menu, set
     * the listener here.
     *
     * @return
     */
    public void setMenuListener(OnMenuListener menuListener) {
        this.menuListener = menuListener;
    }

    public OnMenuListener getMenuListener() {
        return menuListener;
    }

    /**
     * we need the call the method before the menu show, because the padding of
     * activity can't get at the moment of onCreateView();
     */
    private void setViewPadding() {
        this.setPadding(viewActivity.getPaddingLeft(), viewActivity.getPaddingTop(), viewActivity.getPaddingRight(),
                viewActivity.getPaddingBottom());
    }

    /**
     * show the reside menu;
     */
    public void openMenu(int direction) {

        setScaleDirection(direction);

        isOpened = true;
        AnimatorSet scaleDown_activity = buildActivityDownAnimation(viewActivity);
        AnimatorSet scaleDown_menu = buildActivityDownAnimation(scrollViewMenu);
        AnimatorSet scaleDown_shadow = buildActivityDownAnimation(imageViewShadow);
        scaleDown_shadow.addListener(animationListener);
        scaleDown_activity.playTogether(scaleDown_shadow);
        scaleDown_activity.playTogether(scaleDown_menu);
        scaleDown_activity.start();
    }

    /**
     * close the reslide menu;
     */
    public void closeMenu() {

        isOpened = false;
        AnimatorSet scaleUp_activity = buildActivityUpAnimation(viewActivity);
        AnimatorSet scaleUp_menu = buildActivityUpAnimation(scrollViewMenu);
        AnimatorSet scaleUp_shadow = buildActivityUpAnimation(imageViewShadow);
        scaleUp_activity.addListener(animationListener);
        scaleUp_activity.playTogether(scaleUp_shadow);
        scaleUp_activity.playTogether(scaleUp_menu);
        scaleUp_activity.start();
    }

    public void setSwipeDirectionDisable(int direction) {
        disabledSwipeDirection.add(direction);
    }

    private boolean isInDisableDirection(int direction) {
        return disabledSwipeDirection.contains(direction);
    }

    private void setScaleDirection(int direction) {

        if (direction == DIRECTION_LEFT) {
            scrollViewMenu = leftMenu;
        } else {
            scrollViewMenu = rightMenu;
        }

        scaleDirection = direction;
    }

    /**
     * return the flag of menu status;
     *
     * @return
     */
    public boolean isOpened() {
        return isOpened;
    }

    private OnClickListener viewActivityOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (isOpened())
                closeMenu();
        }
    };

    private Animator.AnimatorListener animationListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            if (isOpened()) {
                scrollViewMenu.setVisibility(VISIBLE);
                if (menuListener != null)
                    menuListener.openMenu();
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            // reset the view;
            if (isOpened()) {
                viewActivity.setTouchDisable(true);
                viewActivity.setOnClickListener(viewActivityOnClickListener);
            } else {
                viewActivity.setTouchDisable(false);
                viewActivity.setOnClickListener(null);
                scrollViewMenu.setVisibility(GONE);
                if (menuListener != null)
                    menuListener.closeMenu();
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };

    /**
     * a helper method to build scale down animation;
     *
     * @param target
     * @return
     */
    private AnimatorSet buildActivityDownAnimation(View target) {

        AnimatorSet scaleDown = new AnimatorSet();
        float movex = 0.0f;
        if (target == scrollViewMenu) {

            movex = 0.0f;

        } else {
            movex = (float) (screenWidth * translateXParam);
        }
        scaleDown.play(ObjectAnimator.ofFloat(target, "translationX", movex));

        scaleDown.setInterpolator(AnimationUtils.loadInterpolator(activity, android.R.anim.linear_interpolator));
        scaleDown.setDuration(250);
        return scaleDown;
    }

    /**
     * a helper method to build scale up animation;
     *
     * @param target
     * @return
     */
    private AnimatorSet buildActivityUpAnimation(View target) {

        AnimatorSet scaleUp = new AnimatorSet();
        if (target == scrollViewMenu) {
            scaleUp.play(ObjectAnimator.ofFloat(target, "translationX", -MenuViewWidth * 2));
        } else {
            scaleUp.play(ObjectAnimator.ofFloat(target, "translationX", 0.0f));
        }
        scaleUp.setInterpolator(AnimationUtils.loadInterpolator(activity, android.R.anim.linear_interpolator));
        scaleUp.setDuration(250);
        return scaleUp;
    }

    /**
     * if there ware some view you don't want reside menu to intercept their
     * touch event,you can use the method to set.
     *
     * @param v
     */
    public void addIgnoredView(View v) {
        ignoredViews.add(v);
    }

    /**
     * remove the view from ignored view list;
     *
     * @param v
     */
    public void removeIgnoredView(View v) {
        ignoredViews.remove(v);
    }

    /**
     * clear the ignored view list;
     */
    public void clearIgnoredViewList() {
        ignoredViews.clear();
    }

    /**
     * if the motion evnent was relative to the view which in ignored view
     * list,return true;
     *
     * @param ev
     * @return
     */
    private boolean isInIgnoredView(MotionEvent ev) {
        Rect rect = new Rect();
        for (View v : ignoredViews) {
            v.getGlobalVisibleRect(rect);
            if (rect.contains((int) ev.getX(), (int) ev.getY()))
                return true;
        }
        return false;
    }

    private void setScaleDirectionByRawX(float currentRawX) {

        setScaleDirection(DIRECTION_LEFT);

    }

    private float lastActionDownX, lastActionDownY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        float currentActivityTranslateX = ViewHelper.getTranslationX(viewActivity);
        // System.err.println("the currentActivityTranslateX is " +
        // currentActivityTranslateX);

        setScaleDirectionByRawX(ev.getRawX());

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastActionDownX = ev.getX();
                lastActionDownY = ev.getY();
                isInIgnoredView = isInIgnoredView(ev) && !isOpened();
                pressedState = PRESSED_DOWN;
                break;

            case MotionEvent.ACTION_MOVE:
                if (isInIgnoredView || isInDisableDirection(scaleDirection))
                    break;

                if (pressedState != PRESSED_DOWN && pressedState != PRESSED_MOVE_HORIZANTAL)
                    break;

                int xOffset = (int) (ev.getX() - lastActionDownX);
                int yOffset = (int) (ev.getY() - lastActionDownY);

                if (pressedState == PRESSED_DOWN) {
                    if (yOffset > 25 || yOffset < -25) {
                        pressedState = PRESSED_MOVE_VERTICAL;
                        break;
                    }

                    if (isOpened()) {
                        if (xOffset < -50) {
                            pressedState = PRESSED_MOVE_HORIZANTAL;
                            ev.setAction(MotionEvent.ACTION_CANCEL);
                        }

                    } else {
                        if (xOffset > 50) {
                            pressedState = PRESSED_MOVE_HORIZANTAL;
                            ev.setAction(MotionEvent.ACTION_CANCEL);
                        }
                    }


                } else if (pressedState == PRESSED_MOVE_HORIZANTAL) {

                    if (currentActivityTranslateX < screenWidth * translateXParam) {
                        scrollViewMenu.setVisibility(View.VISIBLE);
                    }

                    float targetTranslateX = ev.getRawX();
                    if (targetTranslateX > screenWidth) {
                        targetTranslateX = screenWidth;
                    }
                    float moveX = targetTranslateX - MenuViewWidth;
                    if (moveX <= 0) {
                        moveX = 0;
                    }

                    ViewHelper.setTranslationX(viewActivity, moveX);
                    ViewHelper.setTranslationX(imageViewShadow, moveX);
                    //这里对scrollViewMenu位移距离乘以2，完全是为了是滑动时，动画效果明显一定，
                    //否则，若MenuViewWidth ，过于窄，将造成动画效果不明显。
                    ViewHelper.setTranslationX(scrollViewMenu, (targetTranslateX / screenWidth - 1) * MenuViewWidth * 2);

                    lastRawX = ev.getRawX();
                    return true;
                }

                break;

            case MotionEvent.ACTION_UP:

                if (isInIgnoredView)
                    break;
                if (pressedState != PRESSED_MOVE_HORIZANTAL)
                    break;

                pressedState = PRESSED_DONE;
                if (isOpened()) {
                    if (currentActivityTranslateX < screenWidth * 0.6) {
                        closeMenu();
                    } else {
                        openMenu(scaleDirection);
                    }
                } else {
                    if (currentActivityTranslateX > screenWidth * 0.4) {
                        openMenu(scaleDirection);
                    } else {
                        closeMenu();
                    }
                }

                break;

        }
        lastRawX = ev.getRawX();
        return super.dispatchTouchEvent(ev);
    }

    public float getTranslateXParam() {
        return translateXParam;
    }

    public void setTranslateXParam(float translateXParam) {
        this.translateXParam = translateXParam;
        this.translateXParam = translateXParam > 1.0f ? 1.0f : translateXParam;
        this.translateXParam = translateXParam < 0.0f ? 0.0f : translateXParam;
        MenuViewWidth = (1 - this.translateXParam) * screenWidth;
    }

    public int getScreenHeight() {
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    public int getScreenWidth() {
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public interface OnMenuListener {

        /**
         * the method will call on the finished time of opening menu's
         * animation.
         */
        public void openMenu();

        /**
         * the method will call on the finished time of closing menu's animation
         * .
         */
        public void closeMenu();
    }

    public void setSettingListener(SettingLayoutListener settingLayoutListener) {
        settingListener = settingLayoutListener;
    }

    public interface SettingLayoutListener {
        public void clickSetting();

        public void clickComment();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.setting:
                if (settingListener != null) {
                    settingListener.clickSetting();
                }
                break;
            case R.id.comment:
                if (settingListener != null) {
                    settingListener.clickComment();
                }
            default:
                break;
        }
    }

}
