package bird.spring.com.myresidemenu;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import bird.spring.com.myresidemenu.residemenu.ResideMenu;
import bird.spring.com.myresidemenu.residemenu.ResideMenuInfo;
import bird.spring.com.myresidemenu.residemenu.ResideMenuItem;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ResideMenu.SettingLayoutListener {

    private ResideMenu resideMenu;
    String[] menuItems;

    private ResideMenuInfo info;

    private boolean is_closed = false;
    private long mExitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        setUpMenu();

    }


    private void setUpMenu() {

        // attach to current activity;
        resideMenu = new ResideMenu(this);
        resideMenu.setSettingListener(this);
        resideMenu.setBackground(R.drawable.menuback);
        resideMenu.attachToActivity(this);
        resideMenu.setMenuListener(menuListener);
        // valid scale factor is between 0.0f and 1.0f. leftmenu'width is
        // 150dip.
        resideMenu.setTranslateXParam(0.85f);
        // 禁止使用右侧菜单
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);

        // create menu items;

        menuItems =new String[] {"开通会员", "QQ钱包", "个性装扮", "我的收藏", "我的相册", "我的文件"};
        int[] icons = {R.drawable.gco, R.drawable.charge_icon, R.drawable.kwz, R.drawable.feo,
                R.drawable.fdh, R.drawable.ept};

        for (int i = 0; i < menuItems.length; i++) {
            ResideMenuItem menuItem = new ResideMenuItem(this, icons[i], menuItems[i]);
            menuItem.setOnClickListener(this);
            menuItem.setId(i);
            resideMenu.addMenuItem(menuItem, ResideMenu.DIRECTION_LEFT);

        }

        info = new ResideMenuInfo(this, R.drawable.fsf, "魑魅魍魉", "32 级");
        resideMenu.addMenuInfo(info);

        info.setOnClickListener(this);


    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.dispatchTouchEvent(ev);
    }

    @Override
    public void onClick(View v) {
        String msg = " ";
        switch (v.getId()) {
            case 0:
                msg = menuItems[0];
                break;
            case 1:
                msg = menuItems[1];
                break;
            case 2:
                msg = menuItems[2];
                break;
            case 3:
                msg = menuItems[3];
                break;
            case 4:
                msg = menuItems[4];
                break;
            case 5:
                msg = menuItems[5];
                break;
            case 6:
                msg = menuItems[6];
                break;
            default:
                msg = "This is default";
                break;
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    private ResideMenu.OnMenuListener menuListener = new ResideMenu.OnMenuListener() {
        @Override
        public void openMenu() {
            is_closed = false;
        }

        @Override
        public void closeMenu() {
            is_closed = true;
        }
    };


    // 监听手机上的BACK键
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 判断菜单是否关闭
            if (is_closed) {
                // 判断两次点击的时间间隔（默认设置为2秒）
                if ((System.currentTimeMillis() - mExitTime) > 2000) {
                    Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();

                    mExitTime = System.currentTimeMillis();
                } else {
                    finish();
                    System.exit(0);
                    super.onBackPressed();
                }
            } else {
                resideMenu.closeMenu();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void clickSetting() {
        // TODO Auto-generated method stub
        Toast.makeText(this, "设置", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void clickComment() {
        // TODO Auto-generated method stub
        Toast.makeText(this, "夜间", Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
