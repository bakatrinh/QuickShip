package dev_t.cs161.quickship;

import github.ankushsachdeva.emojicon.EmojiconGridView.OnEmojiconClickedListener;
import github.ankushsachdeva.emojicon.EmojiconsPopup;
import github.ankushsachdeva.emojicon.emoji.Emojicon;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.daasuu.library.DisplayObject;
import com.daasuu.library.FPSTextureView;
import com.daasuu.library.callback.AnimCallBack;
import com.daasuu.library.drawer.BitmapDrawer;
import com.daasuu.library.drawer.SpriteSheetDrawer;
import com.daasuu.library.drawer.TextDrawer;
import com.daasuu.library.easing.Ease;
import com.daasuu.library.util.Util;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class quickShipActivityMain extends Activity implements Runnable {

    private Point screen = new Point();
    private quickShipActivityMain mActivityMain;
    private volatile boolean initialBoot;
    private volatile boolean running;
    private volatile long timeNow;
    private volatile long timePrevFrame = 0;
    private volatile long timeDelta;
    private Float screenWidth;
    private Float screenHeight;
    private ViewFlipper mainScreenViewFlipper;
    private ViewFlipper playModeFlipper;
    private volatile quickShipModel mGameModel;
    private volatile quickShipViewChooseModeGrid chooseModeGrid;
    private volatile quickShipViewPlayModePlayerGrid playModePlayerGrid;
    private volatile quickShipViewPlayModeOpponentGrid playModeOpponentGrid;
    private Button mPlayModeFireBtn;
    private Button startGame;
    private FrameLayout mChooseModeFrameLayout;
    private FrameLayout mSplashScreenFrameLayout;
    private ImageView mSelectedShip;
    private ImageView mTempSelectedShip;
    private ImageView mShipSize2;
    private ImageView mShipSize3a;
    private ImageView mShipSize3b;
    private ImageView mShipSize4;
    private ImageView mShipSize5;
    private Button mRotateBtn;
    private Button mPlaceBtn;
    private Button mDoneBtn;
    private Button mBluetoothEnableButton;
    private EditText mSplashScreenPlayerName;
    private String mPlayerName;
    private BluetoothAdapter btAdapter;
    private StringBuilder messages;
    private ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    private BluetoothConnectionService mBluetoothConnection;
    private DeviceListAdapter mDeviceListAdapter;
    private BluetoothDevice mBTDevice;
    private AlertDialog mBTListViewDialog;
    private ListView mDevicesListView;
    private ScrollView mChooseModeScroller;
    private TextView mChooseModeChatMessageLog;
    private EditText mChooseModeEditTextSend;
    private ScrollView mPlayModeScroller;
    private TextView mPlayModeChatMessageLog;
    private EditText mPlayModeEditTextSend;
    private TextView mPlayModeStatusText;
    private boolean playerChooseModeDone;
    private boolean opponentChooseModeDone;
    private boolean playerTurnDone;
    private boolean opponentTurnDone;
    private boolean gameOver;
    private boolean debugButtons;
    private int gameOverStatus;
    private int playerChosenTarget;
    private int opponentChosenTarget;
    private String playerChosenEmoji;
    private String opponentChosenEmoji = "\uD83D\uDE00";
    private int turnCount;
    private EmojiconsPopup emojiPopup;
    private FPSTextureView mFPSTextureView;
    private Bitmap mHitText;
    private Bitmap mMissText;
    private int endCode;
    private float mCellWidth;
    private boolean enableAnimation;
    private int animationStage;
    private boolean animating;
    private boolean opponentAnimating;
    private Boolean animateFirst;
    private boolean quitGamePushed;
    private boolean leftGameDisplayedOnce;
    private boolean fireButtonPressed;
    private String playerUUID;
    private String opponentUUID;
    static final int ANIMSTAGE1 = 0;
    static final int ANIMSTAGE2 = 1;
    static final int ANIMSTAGE3 = 2;
    static final int ANIMSTAGE4 = 3;
    static final int ANIMSTAGE5 = 4;
    static final int WON = 0;
    static final int LOST = 1;
    static final int DRAW = 2;
    private BottomNavigationView mBottomNavigation;
    private boolean receivedGameOverState;

    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Display display = getWindowManager().getDefaultDisplay();
        display.getSize(screen);
        screenWidth = (float) screen.x;
        screenHeight = (float) screen.y;
        initialBoot = true;
        initializeView();
        otherViewsInitializeObjects();
        blueToothInitializeObjects();
        launchStartScreen();
        emojiPopUpInitializer();
        bottomNavInitializer();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    public void bottomNavInitializer() {
        mBottomNavigation = (BottomNavigationView) findViewById(R.id.bottom_nav);
        mBottomNavigation.getMenu().getItem(1).setChecked(true);

        mBottomNavigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.item_my_board:
                                if (!animating && !fireButtonPressed) {
                                    if (playModeFlipper.getDisplayedChild() == 1 || playModeFlipper.getDisplayedChild() == 2) {
                                        playModeSwitchToPlayerGrid(null);
                                    }
                                }
                                break;
                            case R.id.item_opponent_board:
                                if (!animating && !fireButtonPressed) {
                                    if (playModeFlipper.getDisplayedChild() == 0 || playModeFlipper.getDisplayedChild() == 2) {
                                        playModeSwitchToOpponentGrid(null);
                                    }
                                }
                                break;
                            case R.id.item_options:
                                if (!animating && !fireButtonPressed) {
                                    if (playModeFlipper.getDisplayedChild() == 0 || playModeFlipper.getDisplayedChild() == 1) {
                                        playModeSwitchToOptions(null);
                                    }
                                }
                                break;
                        }
                        return false;
                    }
                });
    }

    public void initializeView() {
        setContentView(R.layout.quickship_main_screen);
        mActivityMain = this;

        endCode = 0;

        mSplashScreenPlayerName = (EditText) findViewById(R.id.splash_screen_player_name);
        mPlayModeStatusText = (TextView) findViewById(R.id.play_mode_status);
        mChooseModeFrameLayout = (FrameLayout) findViewById(R.id.choose_mode);
        mSplashScreenFrameLayout = (FrameLayout) findViewById(R.id.splash_screen);
        mTempSelectedShip = (ImageView) findViewById(R.id.temp_ship_spot);
        mPlayModeFireBtn = (Button) findViewById(R.id.play_mode_fire_btn);

        mPlayModeFireBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    mPlayModeFireBtn.setBackgroundResource(R.drawable.firebutton_02);
                return false;
            }
        });

        mainScreenViewFlipper = (ViewFlipper) findViewById(R.id.main_screen_view_flipper);
        playModeFlipper = (ViewFlipper) findViewById(R.id.play_mode_view_flipper);

        mShipSize2 = (ImageView) findViewById(R.id.image_view_ship_size_2);
        mShipSize3a = (ImageView) findViewById(R.id.image_view_ship_size_3_a);
        mShipSize3b = (ImageView) findViewById(R.id.image_view_ship_size_3_b);
        mShipSize4 = (ImageView) findViewById(R.id.image_view_ship_size_4);
        mShipSize5 = (ImageView) findViewById(R.id.image_view_ship_size_5);

        mRotateBtn = (Button) findViewById(R.id.choose_mode_rotate_button);
        mPlaceBtn = (Button) findViewById(R.id.choose_mode_place_button);
        mDoneBtn = (Button) findViewById(R.id.choose_mode_done_button);

        mChooseModeScroller = (ScrollView) findViewById(R.id.choose_mode_scroller);
        mChooseModeChatMessageLog = (TextView) findViewById(R.id.edit_text_chat_log);
        mChooseModeEditTextSend = (EditText) findViewById(R.id.edit_text_send_message);

        mPlayModeScroller = (ScrollView) findViewById(R.id.play_mode_scroller);
        mPlayModeChatMessageLog = (TextView) findViewById(R.id.edit_text_chat_log_in_game);
        mPlayModeEditTextSend = (EditText) findViewById(R.id.edit_text_send_message_in_game);

        //Chat Box for Ship Placement Screen
        mChooseModeEditTextSend.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    InputMethodManager inputManager = (InputMethodManager) mActivityMain.getSystemService(mActivityMain.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(mActivityMain.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    String full_msg = getColoredSpanned(mPlayerName + ": " + mChooseModeEditTextSend.getText().toString(), "#000000");
                    messages.append(full_msg + getResources().getString(R.string.play_mode_break_tags_chat));
                    appendToChat();

                    quickShipBluetoothPacketsToBeSent data = new quickShipBluetoothPacketsToBeSent(quickShipBluetoothPacketsToBeSent.CHAT, full_msg);
                    //Log.d("Chat Parcel Byte Size: ",""+ParcelableUtil.marshall(data).length); //debugging
                    mBluetoothConnection.write(ParcelableUtil.marshall(data));
                    mChooseModeEditTextSend.setText("");//clear message
                    return true;//em
                }
                return false;
            }
        });

        //Chat Box for Opponent screen
        mPlayModeEditTextSend.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (!animating) {
                        InputMethodManager inputManager = (InputMethodManager) mActivityMain.getSystemService(mActivityMain.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(mActivityMain.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        String full_msg = getColoredSpanned(mPlayerName + ": " + mPlayModeEditTextSend.getText().toString(), "#000000");
                        messages.append(full_msg + getResources().getString(R.string.play_mode_break_tags_chat));

                        appendToChat();

                        quickShipBluetoothPacketsToBeSent data = new quickShipBluetoothPacketsToBeSent(quickShipBluetoothPacketsToBeSent.CHAT, full_msg);
                        mBluetoothConnection.write(ParcelableUtil.marshall(data));
                        mPlayModeEditTextSend.setText("");//clear message
                    }
                    return true;
                }
                return false;
            }
        });
        mPlayModeEditTextSend.clearFocus();

        startGame = (Button) findViewById(R.id.start_game_btn);
        mBluetoothEnableButton = (Button) findViewById(R.id.splash_creen_bluetooth_btn);

        SharedPreferences preferences = getSharedPreferences("quickShipSettings", MODE_PRIVATE);
        enableAnimation = preferences.getBoolean("enableAnimation", true);

        CheckBox enableAnimationCheckbox = (CheckBox) findViewById(R.id.enable_animation_checkbox);
        if (enableAnimation) {
            enableAnimationCheckbox.setChecked(true);
        } else {
            enableAnimationCheckbox.setChecked(false);
        }

        enableAnimationCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    enableAnimation = true;
                    SharedPreferences preferences = getSharedPreferences("quickShipSettings", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("enableAnimation", true);
                    editor.commit();
                } else {
                    enableAnimation = false;
                    SharedPreferences preferences = getSharedPreferences("quickShipSettings", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("enableAnimation", false);
                    editor.commit();
                }
            }
        });

        mSplashScreenPlayerName.setText(mPlayerName);
    }

    public void otherViewsInitializeObjects() {
        mGameModel = new quickShipModel();
        LinearLayout topLinear = (LinearLayout) findViewById(R.id.choose_mode_top_linear);
        FrameLayout topFrame = (FrameLayout) findViewById(R.id.choose_mode_top_frame);
        chooseModeGrid = new quickShipViewChooseModeGrid(this, mGameModel, mChooseModeFrameLayout, mTempSelectedShip);
        topFrame.getLayoutParams().height = Math.round(screenWidth);
        topFrame.addView(chooseModeGrid);
        FrameLayout topFrameBorder = (FrameLayout) findViewById(R.id.choose_mode_top_frame_border);
        topFrameBorder.addView(new quickShipViewGridBorder(this, ContextCompat.getColor(this, R.color.choose_mode_player_frame_color)));
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Math.round(screenWidth));
        topLinear.setLayoutParams(param);

        LinearLayout topOpponentLinear = (LinearLayout) findViewById(R.id.play_mode_opponent_top_linear);
        FrameLayout topOpponentFrame = (FrameLayout) findViewById(R.id.play_mode_opponent_top_frame);
        playModeOpponentGrid = new quickShipViewPlayModeOpponentGrid(this, mGameModel);
        topOpponentFrame.getLayoutParams().height = Math.round(screenWidth);
        topOpponentFrame.addView(playModeOpponentGrid);

        LinearLayout.LayoutParams param2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Math.round(screenWidth));
        topOpponentLinear.setLayoutParams(param2);
        FrameLayout topOpponentFrameBorder = (FrameLayout) findViewById(R.id.play_mode_opponent_top_frame_border);
        topOpponentFrameBorder.addView(new quickShipViewGridBorder(this, ContextCompat.getColor(this, R.color.play_mode_opponent_frame_color)));

        LinearLayout topPlayerLinear = (LinearLayout) findViewById(R.id.play_mode_player_top_linear);
        FrameLayout topPlayerFrame = (FrameLayout) findViewById(R.id.play_mode_player_top_frame);
        playModePlayerGrid = new quickShipViewPlayModePlayerGrid(this, mGameModel);
        topPlayerFrame.getLayoutParams().height = Math.round(screenWidth);
        topPlayerFrame.addView(playModePlayerGrid);
        LinearLayout.LayoutParams param3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Math.round(screenWidth));
        topPlayerLinear.setLayoutParams(param3);
        FrameLayout topPlayerFrameBorder = (FrameLayout) findViewById(R.id.play_mode_player_top_frame_border);
        topPlayerFrameBorder.addView(new quickShipViewGridBorder(this, ContextCompat.getColor(this, R.color.play_mode_player_frame_color)));

        mFPSTextureView = (FPSTextureView) findViewById(R.id.animation_texture_view);
        mFPSTextureView.tickStart();
    }

    public void emojiPopUpInitializer() {
        LinearLayout root = (LinearLayout) findViewById(R.id.play_mode_opponent_top_linear);
        emojiPopup = new EmojiconsPopup(root, this);

        Double widthWithMargin = screenWidth * 0.9;
        Double heightWithMargin = screenHeight - (screenWidth * 0.1);
        emojiPopup.setSize(widthWithMargin.intValue(), heightWithMargin.intValue());

        emojiPopup.setOnEmojiconClickedListener(new OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {

                playerChosenEmoji = emojicon.getEmoji();
                //Log.d("DEBUG", opponentChosenEmoji);
                emojiPopup.dismiss();
                mPlayModeEditTextSend.setEnabled(true);
                playerChosenTarget = playModeOpponentGrid.getCurrentIndex();

                quickShipBluetoothPacketsToBeSent data = new quickShipBluetoothPacketsToBeSent(quickShipBluetoothPacketsToBeSent.MOVES, playerChosenTarget, getPlayerChosenEmoji());
                mBluetoothConnection.write(ParcelableUtil.marshall(data));
                playerTurnDone = true;
                checkPlayModeTurnDone("player");

            }
        });
    }

    public boolean btAdapterExists() {
        if (btAdapter != null) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isBTon() {
        if (btAdapter.isEnabled()) {
            return true;
        } else {
            return false;
        }
    }

    public void blueToothInitializeObjects() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        messages = new StringBuilder();
        if (btAdapter == null) {
            startGame.setEnabled(false);
            mSplashScreenPlayerName.setVisibility(View.INVISIBLE);//em
            AlertDialog alertDialog = new AlertDialog.Builder(mActivityMain).create();
            alertDialog.setTitle(getResources().getString(R.string.splash_screen_no_bluetooth_error_message_title));
            alertDialog.setMessage(getResources().getString(R.string.splash_screen_no_bluetooth_error_message));
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.app_name_confirmed_btn),
                                  new DialogInterface.OnClickListener() {
                                      public void onClick(DialogInterface dialog, int which) {
                                          dialog.dismiss();
                                      }
                                  });
            alertDialog.show();
        } else if (!btAdapter.isEnabled()) {
            startGame.setEnabled(false);
            mSplashScreenPlayerName.setVisibility(View.INVISIBLE);//em
            mBluetoothEnableButton.setVisibility(View.VISIBLE);
            AlertDialog alertDialog = new AlertDialog.Builder(mActivityMain).create();
            alertDialog.setTitle("Bluetooth Required");
            alertDialog.setMessage(getResources().getString(R.string.splash_screen_bluetooth_alert_message));
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.app_name_confirmed_btn),
                                  new DialogInterface.OnClickListener() {
                                      public void onClick(DialogInterface dialog, int which) {
                                          dialog.dismiss();
                                      }
                                  });
            alertDialog.show();
        }
        mDevicesListView = new ListView(this);
        // Used for receiving quickship parcelables
        //registerReceiver(quickShipDock, new IntentFilter("quickShipCargo"));
        LocalBroadcastManager.getInstance(this).registerReceiver(quickShipDock, new IntentFilter("quickShipCargo"));
        // Used for initial connection of devices
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBtReceiver, filter);
    }

    public void launchStartScreen() {
        SharedPreferences preferences = getSharedPreferences("quickShipSettings", MODE_PRIVATE);
        mPlayerName = preferences.getString("playerName", "");//em
        mSplashScreenPlayerName.setText(mPlayerName);
        mainScreenViewFlipper.setDisplayedChild(0);
        BitmapDrawable background = new BitmapDrawable(scaleDownDrawableImage(R.drawable.ocean_top, Math.round(screenHeight), Math.round(screenWidth)));
        mSplashScreenFrameLayout.setBackgroundDrawable(background);
        startGame.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String playerNameCheck = mSplashScreenPlayerName.getText().toString();
                if (playerNameCheck.matches("")) {
                    Toast.makeText(mActivityMain, getResources().getString(R.string.splash_screen_player_name_error_message), Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    startBTListViewDialog();
                    //newGame();
                }
            }
        });
    }

    public void startBTListViewDialog() {
        String playerNameCheck = mSplashScreenPlayerName.getText().toString();
        if (!playerNameCheck.equals(mPlayerName)) {
            mPlayerName = playerNameCheck;
            SharedPreferences preferences = getSharedPreferences("quickShipSettings", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("playerName", playerNameCheck);
            editor.commit();
        }

        if (btAdapter.setName("QSBT_" + mPlayerName)) {
            Intent discoverableIntent =
                    new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);

            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            registerReceiver(mBtReceiver, filter);

            startActivity(discoverableIntent);

            func_alertDisplayBTDevices();
        }
    }

    // This is required to avoid out of memory issues from loading large images
    public void loadChooseModeBitmaps(String tag, int layoutHeight, int layoutWidth) {

        switch (tag) {
            case "image_view_ship_size_2":
                mShipSize2.setImageBitmap(scaleDownDrawableImage(R.drawable.ship_size2_horizontal, layoutHeight, layoutWidth));
                break;

            case "image_view_ship_size_3_a":
                mShipSize3a.setImageBitmap(scaleDownDrawableImage(R.drawable.ship_size3_a_horizontal, layoutHeight, layoutWidth));
                break;

            case "image_view_ship_size_3_b":
                mShipSize3b.setImageBitmap(scaleDownDrawableImage(R.drawable.ship_size3_b_horizontal, layoutHeight, layoutWidth));
                break;

            case "image_view_ship_size_4":
                mShipSize4.setImageBitmap(scaleDownDrawableImage(R.drawable.ship_size4_horizontal, layoutHeight, layoutWidth));
                break;

            case "image_view_ship_size_5":
                mShipSize5.setImageBitmap(scaleDownDrawableImage(R.drawable.ship_size5_horizontal, layoutHeight, layoutWidth));
                break;

            case "splash_screen_parent":
                ImageView quickship_logo_img = (ImageView) findViewById(R.id.quickship_logo_img);
                quickship_logo_img.setImageBitmap(scaleDownDrawableImage(R.drawable.quickship_splashscreen, layoutHeight, layoutWidth));
                break;

            case "team_logo_parent":
                ImageView company_logo = (ImageView) findViewById(R.id.company_logo);
                company_logo.setImageBitmap(scaleDownDrawableImage(R.drawable.company_logo_black, layoutHeight, layoutWidth));
                break;
        }
    }

    public void setPlayModeFireBtnStatus(boolean status) {
        mPlayModeFireBtn.setEnabled(status);
        if (status == false) {
            mPlayModeFireBtn.setBackgroundResource(R.drawable.firebutton_01_disabled);
        }
    }

    public void setButtonBack(boolean on) {
        if (on == true) {
            mPlayModeFireBtn.setBackgroundResource(R.drawable.firebutton_01);
        } else {
            mPlayModeFireBtn.setBackgroundResource(R.drawable.firebutton_01_disabled);
        }
    }

    public void setChooseModeRotateBtnStatus(boolean status) {
        mRotateBtn.setEnabled(status);
    }

    public void setChooseModePlaceBtnStatus(boolean status) {
        mPlaceBtn.setEnabled(status);
    }

    public void setChooseModeDoneBtnStatus(boolean status) {
        mDoneBtn.setEnabled(status);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!initialBoot) {
            reinitializeUI();
        } else {
            initialBoot = false;
        }
        if (animating) {
            mFPSTextureView.tickStart();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFPSTextureView.tickStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!initialBoot) {
            reinitializeUI();
        } else {
            initialBoot = false;
        }
    }

    public void reinitializeUI() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (playModeFlipper.getDisplayedChild() == 0) {
//                    //Set Player Item to checked
//                } else if (playModeFlipper.getDisplayedChild() == 1) {
//                    //Set opponent item to checked
//                } else {
//                    //Set Options menu to checked
//                }
//            }
//        });
    }

    public void newGame() {
        gameOver = false;
        debugButtons = false;
        quitGamePushed = false;
        leftGameDisplayedOnce = false;
        fireButtonPressed = false;
        receivedGameOverState = false;
        gameOverStatus = 3;
        turnCount = 1;

        animateFirst = null;
        playerUUID = btAdapter.getAddress();

        opponentUUID = "";
        messages.setLength(0);
        mChooseModeChatMessageLog.setText(getResources().getString(R.string.choose_mode_chat_message_default_message));
        mPlayModeChatMessageLog.setText("");
        animating = false;
        playerChooseModeDone = false;
        opponentChooseModeDone = false;
        opponentAnimating = false;
        playerTurnDone = false;
        opponentTurnDone = false;
        mPlayModeStatusText.setVisibility(View.INVISIBLE);
        mPlayModeFireBtn.setEnabled(false);
        mPlayModeFireBtn.setBackgroundResource(R.drawable.firebutton_01_disabled);
        mGameModel = new quickShipModel();
        chooseModeGrid.setGameModel(mGameModel);
        playModeOpponentGrid.setGameModel(mGameModel);
        playModePlayerGrid.setGameModel(mGameModel);
        mPlayModeFireBtn.setText("");
        mPlayModeStatusText.setText("");
        chooseModeGrid.invalidate();
        playModeOpponentGrid.invalidate();
        playModePlayerGrid.invalidate();
        //mPlayModeFireBtn.setText("Fire!");
    }

    public void switchToPlayModeScreen(View view) {
        mainScreenViewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.in_from_right));
        mainScreenViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.out_from_left));
        mainScreenViewFlipper.setDisplayedChild(2);
    }

    public void switchToChooseModeScreen(View view) {
        mainScreenViewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.in_from_left));
        mainScreenViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.out_from_right));
        mainScreenViewFlipper.setDisplayedChild(1);
    }

    public void switchToSplashScreen(View view) {
        mainScreenViewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.in_from_left));
        mainScreenViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.out_from_right));
        mainScreenViewFlipper.setDisplayedChild(0);
    }

    public void playModeSwitchToPlayerGrid(View view) {
        if (!animating) {
            if (playModeFlipper.getDisplayedChild() == 1 || playModeFlipper.getDisplayedChild() == 2) {
                playModeFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.in_from_left));
                playModeFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.out_from_right));
                playModeFlipper.setDisplayedChild(0);
                mBottomNavigation.getMenu().getItem(0).setChecked(true);
                mBottomNavigation.getMenu().getItem(1).setChecked(false);
                mBottomNavigation.getMenu().getItem(2).setChecked(false);
            }
        }
    }

    public void playModeSwitchToOpponentGrid(View view) {
        if (!animating) {
            if (playModeFlipper.getDisplayedChild() == 0 || playModeFlipper.getDisplayedChild() == 2) {
                if (playModeFlipper.getDisplayedChild() == 0) {
                    playModeFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.in_from_right));
                    playModeFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.out_from_left));
                } else {
                    playModeFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.in_from_left));
                    playModeFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.out_from_right));
                }
                playModeFlipper.setDisplayedChild(1);
                mBottomNavigation.getMenu().getItem(1).setChecked(true);
                mBottomNavigation.getMenu().getItem(0).setChecked(false);
                mBottomNavigation.getMenu().getItem(2).setChecked(false);
            }
        }
    }

    public void playModeSwitchToOptions(View view) {
        if (!animating) {
            if (playModeFlipper.getDisplayedChild() == 0 || playModeFlipper.getDisplayedChild() == 1) {
                playModeFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.in_from_right));
                playModeFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.out_from_left));
                playModeFlipper.setDisplayedChild(2);
                mBottomNavigation.getMenu().getItem(2).setChecked(true);
                mBottomNavigation.getMenu().getItem(0).setChecked(false);
                mBottomNavigation.getMenu().getItem(1).setChecked(false);
            }
        }
    }

    public void setChooseModeSelectedShip(View selectedShip) {
        if (!playerChooseModeDone) {
            mShipSize2.setBackgroundColor(0);
            mShipSize3a.setBackgroundColor(0);
            mShipSize3b.setBackgroundColor(0);
            mShipSize4.setBackgroundColor(0);
            mShipSize5.setBackgroundColor(0);

            if (mSelectedShip == null || (selectedShip != null && !mSelectedShip.equals(selectedShip))) {
                selectedShip.setBackgroundColor(ContextCompat.getColor(this, R.color.choose_mode_ship_selected));
                mSelectedShip = (ImageView) selectedShip;
                String shipTag = (String) mSelectedShip.getTag();
                switch (shipTag) {
                    case "image_view_ship_size_2":
                        chooseModeGrid.setShipSelected(quickShipModelBoardSlot.TWO);
                        changePlacedShipsBitmaps();
                        mShipSize2.setImageBitmap(scaleDownDrawableImage(R.drawable.ship_size2_horizontal, mShipSize2.getHeight(), mShipSize2.getWidth()));
                        break;

                    case "image_view_ship_size_3_a":
                        chooseModeGrid.setShipSelected(quickShipModelBoardSlot.THREE_A);
                        changePlacedShipsBitmaps();
                        mShipSize3a.setImageBitmap(scaleDownDrawableImage(R.drawable.ship_size3_a_horizontal, mShipSize3a.getHeight(), mShipSize3a.getWidth()));
                        break;

                    case "image_view_ship_size_3_b":
                        chooseModeGrid.setShipSelected(quickShipModelBoardSlot.THREE_B);
                        changePlacedShipsBitmaps();
                        mShipSize3b.setImageBitmap(scaleDownDrawableImage(R.drawable.ship_size3_b_horizontal, mShipSize3b.getHeight(), mShipSize3b.getWidth()));
                        break;

                    case "image_view_ship_size_4":
                        chooseModeGrid.setShipSelected(quickShipModelBoardSlot.FOUR);
                        changePlacedShipsBitmaps();
                        mShipSize4.setImageBitmap(scaleDownDrawableImage(R.drawable.ship_size4_horizontal, mShipSize4.getHeight(), mShipSize4.getWidth()));
                        break;

                    case "image_view_ship_size_5":
                        chooseModeGrid.setShipSelected(quickShipModelBoardSlot.FIVE);
                        changePlacedShipsBitmaps();
                        mShipSize5.setImageBitmap(scaleDownDrawableImage(R.drawable.ship_size5_horizontal, mShipSize5.getHeight(), mShipSize5.getWidth()));
                        break;
                }
            } else {
                mSelectedShip = null;
                chooseModeGrid.deSelectShip();
            }
        }
    }

    public void placeButton(View button) {
        mShipSize2.setBackgroundColor(0);
        mShipSize3a.setBackgroundColor(0);
        mShipSize3b.setBackgroundColor(0);
        mShipSize4.setBackgroundColor(0);
        mShipSize5.setBackgroundColor(0);
        mSelectedShip = null;
        chooseModeGrid.deSelectShip();
        changePlacedShipsBitmaps();
    }

    public void changePlacedShipsBitmaps() {
        for (int i = 0; i < 100; i++) {
            if (mGameModel.getPlayerGameBoard().getShipSlotAtIndex(i).isOccupied() && mGameModel.getPlayerGameBoard().getShipSlotAtIndex(i).isAnchor()) {
                quickShipModelBoardSlot currentShip = mGameModel.getPlayerGameBoard().getShipSlotAtIndex(i);
                switch (currentShip.getShipType()) {
                    case quickShipModelBoardSlot.TWO:
                        mShipSize2.setImageBitmap(scaleDownDrawableImage(R.drawable.ship_size2_01_used, mShipSize2.getHeight(), mShipSize2.getWidth()));
                        break;

                    case quickShipModelBoardSlot.THREE_A:
                        mShipSize3a.setImageBitmap(scaleDownDrawableImage(R.drawable.ship_size3_01_used, mShipSize3a.getHeight(), mShipSize3a.getWidth()));
                        break;

                    case quickShipModelBoardSlot.THREE_B:
                        mShipSize3b.setImageBitmap(scaleDownDrawableImage(R.drawable.ship_size3_02_used, mShipSize3b.getHeight(), mShipSize3b.getWidth()));
                        break;

                    case quickShipModelBoardSlot.FOUR:
                        mShipSize4.setImageBitmap(scaleDownDrawableImage(R.drawable.ship_size4_01_used, mShipSize4.getHeight(), mShipSize4.getWidth()));
                        break;

                    case quickShipModelBoardSlot.FIVE:
                        mShipSize5.setImageBitmap(scaleDownDrawableImage(R.drawable.ship_size5_01_used, mShipSize5.getHeight(), mShipSize5.getWidth()));
                        break;
                }
            }
        }
    }

    public void doneButton(View button) {
        setChooseModeDoneBtnStatus(false);
        //String x = mGameModel.convertPlayerBoardToGSON();
        byte[] x = mGameModel.convertPlayerBoardToByteArray();
        Log.d("BOARD SIZE: ", "" + x.length);
        //quickShipBluetoothPacketsToBeSent data = new quickShipBluetoothPacketsToBeSent(quickShipBluetoothPacketsToBeSent.SHIPS_PLACED, mGameModel.convertPlayerBoardToGSON());
        quickShipBluetoothPacketsToBeSent data = new quickShipBluetoothPacketsToBeSent(quickShipBluetoothPacketsToBeSent.SHIPS_PLACED, mGameModel.convertPlayerBoardToByteArray());
        Log.d("Final Parcel Size: ", "" + ParcelableUtil.marshall(data).length);
        mBluetoothConnection.write(ParcelableUtil.marshall(data));
        playerChooseModeDone = true;
        checkChooseModeDone("player");
    }

    public void checkChooseModeDone(String status) {
        if (playerChooseModeDone && opponentChooseModeDone) {

            if (animateFirst == null) {
                setAnimateFirst();
            }

            mainScreenViewFlipper.setDisplayedChild(2);
            cachePlayModeViews();
            reinitializeUI();
        } else {
            if (status.equals("player")) {
                String msg = getColoredSpanned(getResources().getString(R.string.choose_mode_chat_player_ready_message), "#eda136");
                messages.append(msg + getResources().getString(R.string.play_mode_break_tags_chat));
                appendToChat();
            } else {
                String msg = getColoredSpanned(getResources().getString(R.string.choose_mode_chat_opponent_ready_message), "#eda136");
                messages.append(msg + getResources().getString(R.string.play_mode_break_tags_chat));
                appendToChat();
            }
        }
    }

    //Update opponent grid when user presses fire button
    public void fireOpponentBtn(View v) {
        if (!gameOver) {
            fireButtonPressed = true;
            mPlayModeEditTextSend.setEnabled(false);
            emojiPopup.showAtBottom();
        } else {
            quitGamePushed = true;
            quitGame();
        }
    }

    public void quitGame() {
        gameOver = false;
        animating = false;
        playerChooseModeDone = false;
        opponentChooseModeDone = false;
        playerTurnDone = false;
        opponentTurnDone = false;
        mFPSTextureView.removeAllChildren();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switchToSplashScreen(null);
                        mBluetoothConnection.disconnect_threads();
                    }
                });
            }
        }, 800, TimeUnit.MILLISECONDS);
    }

    public void quitGameBtn(View v) {
        quitGamePushed = true;

        quickShipBluetoothPacketsToBeSent data = new quickShipBluetoothPacketsToBeSent(quickShipBluetoothPacketsToBeSent.QUIT, true);
        mBluetoothConnection.write(ParcelableUtil.marshall(data));

        gameOver = false;
        animating = false;
        playerChooseModeDone = false;
        opponentChooseModeDone = false;
        playerTurnDone = false;
        opponentTurnDone = false;
        messages.setLength(0);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switchToSplashScreen(null);
                        mBluetoothConnection.disconnect_threads();
                    }
                });
            }
        }, 800, TimeUnit.MILLISECONDS);
    }

    public void receivedAnimationStatus() {
        opponentAnimating = false;
        if (!enableAnimation) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPlayModeStatusText.setText("");
                    mPlayModeStatusText.setVisibility(View.INVISIBLE);
                }
            });
            nextAnimation();
        }
    }

    public void setAnimateFirst() {
        if (playerUUID.compareTo(opponentUUID) > 0) {
            animateFirst = true;
        } else {
            animateFirst = false;
        }
    }

    public void receivedOpponentUUID(String mUUID) {
        this.opponentUUID = mUUID;
        UUID tempPlayerUUID = UUID.fromString(playerUUID);
        UUID tempOpponentUUID = UUID.fromString(opponentUUID);
        if (tempPlayerUUID.compareTo(tempOpponentUUID) > 0) {
            animateFirst = true;
        } else {
            animateFirst = false;
        }
        //Log.d("DEBUGUUID", "playerUUID: "+playerUUID+", opponentUUID: "+opponentUUID);
        //Log.d("DEBUGUUID", "(tempPlayerUUID.compareTo(tempOpponentUUID) = "+tempPlayerUUID.compareTo(tempOpponentUUID));
    }

    public void checkPlayModeTurnDone(String status) {
        if (playerTurnDone && opponentTurnDone) {
            if (enableAnimation) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        animating = true;
                        opponentAnimating = false;
                        animationStage = 0;
                        mPlayModeStatusText.setVisibility(View.INVISIBLE);
                        mPlayModeEditTextSend.setEnabled(false);
                        playModeOpponentGrid.deSelectCell();
                        playModeOpponentGrid.invalidate();
                        mFPSTextureView.removeAllChildren();
                        mFPSTextureView.tickStart();
                        nextAnimation();
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        animating = true;
                        opponentAnimating = true;
                        animationStage = 2;
                        mPlayModeStatusText.setVisibility(View.INVISIBLE);
                        mPlayModeEditTextSend.setEnabled(false);
                        playModeOpponentGrid.deSelectCell();
                        refreshOpponentBoard();
                        refreshPlayerBoard();
                        mFPSTextureView.removeAllChildren();
                        mFPSTextureView.tickStart();
                        nextAnimation();
                    }
                });
            }
        } else {
            mPlayModeFireBtn.setEnabled(false);
            if (status.equals("player")) {
                mPlayModeStatusText.setText(getResources().getString(R.string.play_mode_waiting_opponent_status));
                mPlayModeStatusText.setVisibility(View.VISIBLE);
                mPlayModeFireBtn.setBackgroundResource(R.drawable.firebutton_02);
            } else {
                mPlayModeStatusText.setText(getResources().getString(R.string.play_mode_waiting_player_status));
                mPlayModeStatusText.setVisibility(View.VISIBLE);
            }
        }
    }

    public void animationReset() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mFPSTextureView.removeAllChildren();
                        mFPSTextureView.clearAnimation();
                        mFPSTextureView.refreshDrawableState();
                    }
                });
            }
        }, 100, TimeUnit.MILLISECONDS);
    }

    public void nextAnimation() {
        if (animateFirst == null) {
            setAnimateFirst();
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    nextAnimation();
                }
            }, 400, TimeUnit.MILLISECONDS);
        } else {
            switch (animationStage) {
                case quickShipActivityMain.ANIMSTAGE1:
                    if (animateFirst) {
                        startPlayerBoardAnimation();
                    } else {
                        startOpponentBoardAnimation();
                    }
                    break;
                case quickShipActivityMain.ANIMSTAGE2:
                    if (animateFirst) {
                        startOpponentBoardAnimation();
                    } else {
                        startPlayerBoardAnimation();
                    }
                    break;
                case quickShipActivityMain.ANIMSTAGE3:
                    quickShipBluetoothPacketsToBeSent data = new quickShipBluetoothPacketsToBeSent(quickShipBluetoothPacketsToBeSent.ANIMATIONDONE, true);
                    mBluetoothConnection.write(ParcelableUtil.marshall(data));
                    if (!opponentAnimating) {
                        animationStage++;
                        nextAnimation();
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mPlayModeStatusText.setText(getResources().getString(R.string.play_mode_waiting_opponent_status));
                                mPlayModeStatusText.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                    break;
                case quickShipActivityMain.ANIMSTAGE4:
                    boolean playerGameOver = mGameModel.getPlayerGameBoard().checkGameOver();
                    boolean opponentGameOver = mGameModel.getOpponentGameBoard().checkGameOver();
                    if (!debugButtons && !playerGameOver && !opponentGameOver) {
                        createNewTurnMsgBitmap();
                    } else {
                        gameOver = true;
                        if (!debugButtons) {
                            if (playerGameOver && opponentGameOver) {
                                gameOverStatus = quickShipActivityMain.DRAW;
                            } else if (playerGameOver) {
                                gameOverStatus = quickShipActivityMain.LOST;
                            } else {
                                gameOverStatus = quickShipActivityMain.WON;
                            }
                        }
                        animationStage++;
                        startGameOverAnimation();
                        nextAnimation();
                    }
                    break;
                case quickShipActivityMain.ANIMSTAGE5:
                    startNextTurn();
                    break;
            }
        }
    }

    public void winGame(View v) {
        if (!gameOver) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!receivedGameOverState) {
                        quickShipBluetoothPacketsToBeSent data = new quickShipBluetoothPacketsToBeSent(quickShipBluetoothPacketsToBeSent.GAME_LOST, true);
                        mBluetoothConnection.write(ParcelableUtil.marshall(data));
                    }
                    gameOver = true;
                    debugButtons = true;
                    gameOverStatus = quickShipActivityMain.WON;
                    animating = true;
                    opponentAnimating = false;
                    animationStage = 2;
                    mPlayModeStatusText.setVisibility(View.INVISIBLE);
                    mPlayModeEditTextSend.setEnabled(false);
                    playModeOpponentGrid.deSelectCell();
                    playModeOpponentGrid.invalidate();
                    nextAnimation();
                }
            });
        }
    }

    public void loseGame(View v) {
        if (!gameOver) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!receivedGameOverState) {
                        quickShipBluetoothPacketsToBeSent data = new quickShipBluetoothPacketsToBeSent(quickShipBluetoothPacketsToBeSent.GAME_WON, true);
                        mBluetoothConnection.write(ParcelableUtil.marshall(data));
                    }
                    gameOver = true;
                    debugButtons = true;
                    gameOverStatus = quickShipActivityMain.LOST;
                    animating = true;
                    opponentAnimating = false;
                    animationStage = 2;
                    mPlayModeStatusText.setVisibility(View.INVISIBLE);
                    mPlayModeEditTextSend.setEnabled(false);
                    playModeOpponentGrid.deSelectCell();
                    playModeOpponentGrid.invalidate();
                    nextAnimation();
                }
            });
        }
    }

    public void drawGame(View v) {
        if (!gameOver) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!receivedGameOverState) {
                        quickShipBluetoothPacketsToBeSent data = new quickShipBluetoothPacketsToBeSent(quickShipBluetoothPacketsToBeSent.GAME_DRAW, true);
                        mBluetoothConnection.write(ParcelableUtil.marshall(data));
                    }
                    gameOver = true;
                    debugButtons = true;
                    gameOverStatus = quickShipActivityMain.DRAW;
                    animating = true;
                    opponentAnimating = false;
                    animationStage = 2;
                    mPlayModeStatusText.setVisibility(View.INVISIBLE);
                    mPlayModeEditTextSend.setEnabled(false);
                    playModeOpponentGrid.deSelectCell();
                    playModeOpponentGrid.invalidate();
                    nextAnimation();
                }
            });
        }
    }

    public boolean isPlayerChooseModeDone() {
        return playerChooseModeDone;
    }

    public void setPlayerChooseModeDone(boolean playerChooseModeDone) {
        this.playerChooseModeDone = playerChooseModeDone;
    }

    public boolean isOpponentChooseModeDone() {
        return opponentChooseModeDone;
    }

    public void setOpponentChooseModeDone(boolean opponentChooseModeDone) {
        this.opponentChooseModeDone = opponentChooseModeDone;
    }

    public boolean isPlayerTurnDone() {
        return playerTurnDone;
    }

    public void enableBluetooth(View button) {
        toast_displayMessage(getResources().getString(R.string.communication_blueooth_attempt));

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        IntentFilter BlueToothfilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBtReceiver, BlueToothfilter);

        int REQUEST_ENABLE_BT = 1;
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    public void enableBluetooth() {
        //toast_displayMessage("Attempting to enable Bluetooth...");
        Log.d("UnitTesting", "Attempting to enable Bluetooth...");
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        IntentFilter BlueToothfilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBtReceiver, BlueToothfilter);

        int REQUEST_ENABLE_BT = 1;
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    public void setRotation(View button) {
        chooseModeGrid.setOrientation();
    }

    public Bitmap scaleDownDrawableImage(int res, int reqHeight, int reqWidth) {
        Bitmap b = null;

        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(getResources(), res, o);

        int inSampleSize = 1;

        if (o.outHeight > reqHeight || o.outWidth > reqWidth) {

            final int halfHeight = o.outHeight / 2;
            final int halfWidth = o.outWidth / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inScaled = false;
        o2.inSampleSize = inSampleSize;
        b = BitmapFactory.decodeResource(getResources(), res, o2);

        return b;
    }

    private void toast_displayMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private String getColoredSpanned(String text, String color) {
        return "<font color=" + color + ">" + text + "</font>";
    }

    private final BroadcastReceiver quickShipDock = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras().getParcelable("quickShipPackage") != null) {
                quickShipBluetoothPacketsToBeSent data = intent.getExtras().getParcelable("quickShipPackage");
                int packetType = data.getPacketType();
                Log.d("DEBUG", "PACKETTYPE RECEIVED: " + packetType);
                switch (packetType) {
                    case quickShipBluetoothPacketsToBeSent.CHAT:
                        String text = data.getChatMessage();
                        messages.append(text + getResources().getString(R.string.play_mode_break_tags_chat));
                        appendToChat();
                        break;
                    case quickShipBluetoothPacketsToBeSent.SHIPS_PLACED:
                        byte[] tempBoard = data.getBoardv2();
                        Log.d("DEBUG - received Board", "" + tempBoard.length);
                        mGameModel.setOpponentBoardFromByteArray(tempBoard);
                        opponentChooseModeDone = true;
                        checkChooseModeDone("opponent");
                        break;

                    case quickShipBluetoothPacketsToBeSent.MOVES:
                        opponentChosenTarget = data.getMovesChosen();
                        opponentChosenEmoji = data.getEmojiType();
                        opponentTurnDone = true;
                        checkPlayModeTurnDone("opponent");
                        break;

                    case quickShipBluetoothPacketsToBeSent.ANIMATIONDONE:
                        if (!debugButtons) {
                            receivedAnimationStatus();
                        }
                        break;

                    case quickShipBluetoothPacketsToBeSent.UUID:
                        break;

                    case quickShipBluetoothPacketsToBeSent.TURN_DONE:
                        break;

                    case quickShipBluetoothPacketsToBeSent.GAME_WON:
                        if (!debugButtons) {
                            receivedGameOverState = true;
                            winGame(null);
                        }
                        break;

                    case quickShipBluetoothPacketsToBeSent.GAME_LOST:
                        if (!debugButtons) {
                            receivedGameOverState = true;
                            loseGame(null);
                        }
                        break;

                    case quickShipBluetoothPacketsToBeSent.GAME_DRAW:
                        if (!debugButtons) {
                            receivedGameOverState = true;
                            drawGame(null);
                        }
                        break;

                    case quickShipBluetoothPacketsToBeSent.QUIT:
                        gameOver = true;
                        quitGamePushed = false;
                        leftGameDisplayedOnce = false;
                        break;

                    case quickShipBluetoothPacketsToBeSent.NAME_CHANGE:
                        break;

                    case quickShipBluetoothPacketsToBeSent.DISCONNECTED:
                        if (gameOver && !quitGamePushed && !leftGameDisplayedOnce) {
                            leftGameDisplayedOnce = true;
                            mBluetoothConnection.disconnect_threads();
                            String msg = getColoredSpanned(getResources().getString(R.string.play_mode_divider_chat), "#349edb");
                            messages.append(msg + getResources().getString(R.string.play_mode_break_tags_chat));
                            String msg2 = getColoredSpanned(getResources().getString(R.string.play_mode_opponent_left_chat), "#349edb");
                            messages.append(msg2 + getResources().getString(R.string.play_mode_break_tags_chat));
                            appendToChat();
                            mPlayModeEditTextSend.setEnabled(false);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mPlayModeFireBtn.setBackgroundResource(R.drawable.firebutton_01);
                                    mPlayModeFireBtn.setText(getResources().getString(R.string.game_over_play_again_btn));
                                    mPlayModeFireBtn.setEnabled(true);
                                }
                            });
                        } else {
                            if (!quitGamePushed && !leftGameDisplayedOnce) {
                                quitGame();
                                AlertDialog alertDialog = new AlertDialog.Builder(mActivityMain).create();
                                alertDialog.setTitle(getResources().getString(R.string.play_mode_opponent_disconnect_message_title));
                                alertDialog.setMessage(getResources().getString(R.string.play_mode_opponent_disconnect_message));
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.app_name_confirmed_btn),
                                                      new DialogInterface.OnClickListener() {
                                                          public void onClick(DialogInterface dialog, int which) {
                                                              dialog.dismiss();
                                                          }
                                                      });
                                alertDialog.show();
                            }
                        }
                        break;
                }
            } else if (intent.getBooleanExtra("startGame", false)) {
                Log.d("MainActivity ->", "startGame triggered.");
                mBTListViewDialog.dismiss();
                newGame();
                mainScreenViewFlipper.setDisplayedChild(1);
                //Notify Second Player to start Game.
                quickShipBluetoothPacketsToBeSent data = new quickShipBluetoothPacketsToBeSent(quickShipBluetoothPacketsToBeSent.TURN_DONE, true);
                mBluetoothConnection.write(ParcelableUtil.marshall(data));

                //set opponent unique identifier
                opponentUUID = mBluetoothConnection.opponentMAC();
                /*
                if (animateFirst == null) {
                    quickShipBluetoothPacketsToBeSent data2 = new quickShipBluetoothPacketsToBeSent(quickShipBluetoothPacketsToBeSent.UUID, playerUUID);
                    mBluetoothConnection.write(ParcelableUtil.marshall(data2));
                }*/

            }
        }
    };

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mBtReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // Start Game pressed; Discovering Devices
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() != null && device.getName().contains("QSBT_")) {
                    Boolean duplicate = false;
                    for (int i = 0; i < mBTDevices.size(); i++) {
                        String tempDeviceMac = device.getAddress();
                        if (mBTDevices.get(i).getAddress().equals(tempDeviceMac)) {
                            duplicate = true;
                            break;
                        }
                    }
                    if (!duplicate) {
                        mBTDevices.add(device);
                    }
                }
                Log.d("Discovered Device: ", "" + device.getName());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.quickship_device_adapter_view, mBTDevices);
                mDevicesListView.setAdapter(mDeviceListAdapter);
                mDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        btAdapter.cancelDiscovery();
                        String deviceName = mBTDevices.get(i).getName();
                        String deviceMAC = mBTDevices.get(i).getAddress();
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            toast_displayMessage("Attempting to bond with...\n" + deviceName + "\n" + deviceMAC);
                            if (mBTDevices.get(i).getBondState() == BluetoothDevice.BOND_BONDED) {
                                Log.d("Bonding With", "On click " + mBTDevices.get(i).getName());
                                mBTDevice = mBTDevices.get(i);
                                mBluetoothConnection = new BluetoothConnectionService(mActivityMain);
                                startConnection();
                            } else {
                                mBTDevices.get(i).createBond();
                            }

                        }
                    }
                });
            }
            // Discoverability enabled
            else if (action.equals(btAdapter.ACTION_SCAN_MODE_CHANGED)) {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, btAdapter.ERROR);
                switch (mode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        String message = "Discoverability Enabled.\nDevice name: " + btAdapter.getName() + "\nDevice MAC: " + btAdapter.getAddress();
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        toast_displayMessage("Connecting...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        toast_displayMessage("Connected.");
                        break;
                }
            }
            // Check if bluetooth has been toggled on or off
            else if (action.equals(btAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, btAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        toast_displayMessage("Bluetooth Off.");
                        startGame.setEnabled(false);
                        mSplashScreenPlayerName.setVisibility(View.INVISIBLE);//em
                        mBluetoothEnableButton.setVisibility(View.VISIBLE);
                        AlertDialog alertDialog = new AlertDialog.Builder(mActivityMain).create();
                        alertDialog.setTitle("Bluetooth Required");
                        alertDialog.setMessage(getResources().getString(R.string.splash_screen_bluetooth_alert_message));
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                              new DialogInterface.OnClickListener() {
                                                  public void onClick(DialogInterface dialog, int which) {
                                                      dialog.dismiss();
                                                  }
                                              });
                        alertDialog.show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        toast_displayMessage("Bluetooth Turning Off...");
                        startGame.setEnabled(false);
                        mBluetoothEnableButton.setVisibility(View.VISIBLE);
                        break;
                    case BluetoothAdapter.STATE_ON:
                        String name = btAdapter.getName();
                        String mac = btAdapter.getAddress();
                        Toast.makeText(mActivityMain, "Bluetooth On.\nDevice name: " + name + "\nDevice MAC: " + mac, Toast.LENGTH_LONG).show();
                        startGame.setEnabled(true);
                        mSplashScreenPlayerName.setVisibility(View.VISIBLE);//em
                        mBluetoothEnableButton.setVisibility(View.INVISIBLE);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        toast_displayMessage("Bluetooth Turning On...");
                        break;
                }
            }
            // Discovered device Item pressed; Pairing devices
            else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // case1: bonded already
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d("BT Bonding", "BONDED with " + device.getName());
                    AlertDialog alertDialog = new AlertDialog.Builder(mActivityMain).create();
                    alertDialog.setTitle("Devices Successfully Paired");
                    alertDialog.setMessage("Please Reconnect");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                          new DialogInterface.OnClickListener() {
                                              public void onClick(DialogInterface dialog, int which) {
                                                  dialog.dismiss();
                                              }
                                          });
                    alertDialog.show();
                }

                // case 2: creating a bond
                if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    toast_displayMessage("Pairing Devices...");
                    Log.d("BT Bonding", "Bonding with " + device.getName());
                }

                // case 3: disconnecting a bond
                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d("BT Bonding", "Bond NONE with " + device.getName());
                }

            }


        }
    };

    private void startConnection() {
        ((ViewGroup) mDevicesListView.getParent()).removeView(mDevicesListView);
        startBTConnection(mBTDevice, MY_UUID_INSECURE);
    }

    private void startBTConnection(BluetoothDevice device, UUID uuid) {
        mBluetoothConnection.startClient(device, uuid);
    }

    private void func_alertDisplayBTDevices() {
        if (!btAdapter.isDiscovering())
            btAdapter.startDiscovery();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBtReceiver, filter);

        final AlertDialog.Builder ad_displayBTDevices = new AlertDialog.Builder(mActivityMain);
        ad_displayBTDevices.setTitle("Nearby Bluetooth Devices");
        ad_displayBTDevices.setMessage("Select a Device...");
        mDevicesListView.setAdapter(mDeviceListAdapter);
        ad_displayBTDevices.setView(mDevicesListView);
        ad_displayBTDevices.setCancelable(true);
        ad_displayBTDevices.setPositiveButton("Refresh", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (btAdapter.isDiscovering())
                    btAdapter.cancelDiscovery();

                toast_displayMessage("Refreshing...");

                //Clear mBTDevices for fresh scan
                mBTDevices.clear();

                // Lollipop+ may need extra manual permissions check
                //checkBTPermissions();

                btAdapter.startDiscovery();
                ((ViewGroup) mDevicesListView.getParent()).removeView(mDevicesListView);
                //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                //registerReceiver(mBtReceiver, filter);

                func_alertDisplayBTDevices();
            }
        });
        ad_displayBTDevices.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ((ViewGroup) mDevicesListView.getParent()).removeView(mDevicesListView);
                dialogInterface.cancel();
            }
        });
        mBTListViewDialog = ad_displayBTDevices.create();
        mBTListViewDialog.show();
        mDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                btAdapter.cancelDiscovery();
                String deviceName = mBTDevices.get(i).getName();
                String deviceMAC = mBTDevices.get(i).getAddress();

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    toast_displayMessage("Attempting to connect with...\n" + deviceName + "\n" + deviceMAC);

                    if (mBTDevices.get(i).createBond()) {
                        Log.d("BT Create Bond", "True");
                        mBTDevice = mBTDevices.get(i);
                        mBluetoothConnection = new BluetoothConnectionService(mActivityMain);
                        startConnection();
                    }
                }

                mBTListViewDialog.dismiss();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        endCode = 1;
        if (btAdapter != null)
            btAdapter.cancelDiscovery();
        // Don't forget to unregister the ACTION_FOUND receiver.
        if (mBtReceiver != null)
            unregisterReceiver(mBtReceiver);
    }

    public int getEndCode() {
        return endCode;
    }

    @Override
    public void run() {
//        while (running) {
//            //limit the frame rate to maximum 60 frames per second (16 miliseconds)
//            //limit the frame rate to maximum 30 frames per second (32 miliseconds)
//            timeNow = System.currentTimeMillis();
//            timeDelta = timeNow - timePrevFrame;
//            if (timeDelta < 32) {
//                try {
//                    Thread.sleep(32 - timeDelta);
//                } catch (InterruptedException e) {
//
//                }
//            }
//            timePrevFrame = System.currentTimeMillis();
//            boardScreen.render();
//        }
    }

    public String getPlayerChosenEmoji() {
        return playerChosenEmoji;
    }

    public void setPlayerChosenEmoji(String playerChosenEmoji) {
        this.playerChosenEmoji = playerChosenEmoji;
    }

    public String getOpponentChosenEmoji() {
        return opponentChosenEmoji;
    }

    public void setOpponentChosenEmoji(String opponentChosenEmoji) {
        this.opponentChosenEmoji = opponentChosenEmoji;
    }

    // Used to cache the player grid and opponent grid into memory so there
    // won't be lag on initial screen change
    public void cachePlayModeViews() {
        playModeFlipper.setDisplayedChild(0);
        mBottomNavigation.getMenu().getItem(0).setChecked(true);
        mBottomNavigation.getMenu().getItem(1).setChecked(false);
        mBottomNavigation.getMenu().getItem(2).setChecked(false);
        emojiPopup.showAtBottom();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        emojiPopup.dismiss();
                        playModeFlipper.setDisplayedChild(1);
                        mBottomNavigation.getMenu().getItem(1).setChecked(true);
                        mBottomNavigation.getMenu().getItem(0).setChecked(false);
                        mBottomNavigation.getMenu().getItem(2).setChecked(false);
                    }
                });
            }
        }, 200, TimeUnit.MILLISECONDS);

        ScheduledExecutorService scheduler2 = Executors.newScheduledThreadPool(1);
        scheduler2.schedule(new Runnable() {
            @Override
            public void run() {
                messages.setLength(0);
                String msg = getColoredSpanned(getResources().getString(R.string.choose_mode_chat_game_started_message), "#eda136");
                messages.append(msg + getResources().getString(R.string.play_mode_break_tags_chat));
                appendToChat();
            }
        }, 400, TimeUnit.MILLISECONDS);
    }

    public void startPlayerBoardAnimation() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (playModeFlipper.getDisplayedChild() == 1 || playModeFlipper.getDisplayedChild() == 2) {
                            playModeFlipper.setInAnimation(AnimationUtils.loadAnimation(mActivityMain, R.anim.in_from_left));
                            playModeFlipper.setOutAnimation(AnimationUtils.loadAnimation(mActivityMain, R.anim.out_from_right));
                            playModeFlipper.getInAnimation().setAnimationListener(new Animation.AnimationListener() {
                                public void onAnimationStart(Animation animation) {
                                }

                                public void onAnimationRepeat(Animation animation) {
                                }

                                public void onAnimationEnd(Animation animation) {
                                    startPlayerBoardAnimation2();
                                }
                            });
                            playModeFlipper.setDisplayedChild(0);
                            mBottomNavigation.getMenu().getItem(0).setChecked(true);
                            mBottomNavigation.getMenu().getItem(1).setChecked(false);
                            mBottomNavigation.getMenu().getItem(2).setChecked(false);
                        } else {
                            startPlayerBoardAnimation2();
                        }
                    }
                });
            }
        }, 1000, TimeUnit.MILLISECONDS);
    }

    public void startPlayerBoardAnimation2() {
        Boolean hitStatusPlayer = mGameModel.getPlayerGameBoard().getShipSlotAtIndex(opponentChosenTarget).isOccupied();

        float[] slotIndex = playModePlayerGrid.getIndexXYCoord(opponentChosenTarget);
        if (hitStatusPlayer) {
            Float bitmapSize = slotIndex[4];
            Bitmap emojiBitmap = textToBitmap(opponentChosenEmoji, bitmapSize);
            createHitRocketBitmap(slotIndex, opponentChosenTarget, emojiBitmap);
        } else {
            createMissRocketBitmap(slotIndex, opponentChosenTarget);
        }
    }

    public void startOpponentBoardAnimation() {
        long tempDelay;
        if (animationStage == quickShipActivityMain.ANIMSTAGE1 && !animateFirst) {
            tempDelay = 1200;
        } else {
            tempDelay = 1000;
        }
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (playModeFlipper.getDisplayedChild() == 0 || playModeFlipper.getDisplayedChild() == 2) {
                            if (playModeFlipper.getDisplayedChild() == 0) {
                                playModeFlipper.setInAnimation(AnimationUtils.loadAnimation(mActivityMain, R.anim.in_from_right));
                                playModeFlipper.setOutAnimation(AnimationUtils.loadAnimation(mActivityMain, R.anim.out_from_left));
                            } else {
                                playModeFlipper.setInAnimation(AnimationUtils.loadAnimation(mActivityMain, R.anim.in_from_left));
                                playModeFlipper.setOutAnimation(AnimationUtils.loadAnimation(mActivityMain, R.anim.out_from_right));
                            }
                            playModeFlipper.getInAnimation().setAnimationListener(new Animation.AnimationListener() {
                                public void onAnimationStart(Animation animation) {
                                }

                                public void onAnimationRepeat(Animation animation) {
                                }

                                public void onAnimationEnd(Animation animation) {
                                    startOpponentBoardAnimation2();
                                }
                            });
                            playModeFlipper.setDisplayedChild(1);
                            mBottomNavigation.getMenu().getItem(1).setChecked(true);
                            mBottomNavigation.getMenu().getItem(0).setChecked(false);
                            mBottomNavigation.getMenu().getItem(2).setChecked(false);
                        } else {
                            startOpponentBoardAnimation2();
                        }
                    }
                });
            }
        }, tempDelay, TimeUnit.MILLISECONDS);
    }

    public void startOpponentBoardAnimation2() {
        Boolean hitStatusOpponent = mGameModel.getOpponentGameBoard().getShipSlotAtIndex(playerChosenTarget).isOccupied();
        float[] slotIndex = playModeOpponentGrid.getIndexXYCoord(playerChosenTarget);
        if (hitStatusOpponent) {
            Float bitmapSize = slotIndex[4];
            Bitmap emojiBitmap = textToBitmap(playerChosenEmoji, bitmapSize);
            createHitRocketBitmap(slotIndex, playerChosenTarget, emojiBitmap);
        } else {
            createMissRocketBitmap(slotIndex, playerChosenTarget);
        }
    }

    public void startNextTurn() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (playModeFlipper.getDisplayedChild() == 0 || playModeFlipper.getDisplayedChild() == 2) {
                            if (playModeFlipper.getDisplayedChild() == 0) {
                                playModeFlipper.setInAnimation(AnimationUtils.loadAnimation(mActivityMain, R.anim.in_from_right));
                                playModeFlipper.setOutAnimation(AnimationUtils.loadAnimation(mActivityMain, R.anim.out_from_left));
                            } else {
                                playModeFlipper.setInAnimation(AnimationUtils.loadAnimation(mActivityMain, R.anim.in_from_left));
                                playModeFlipper.setOutAnimation(AnimationUtils.loadAnimation(mActivityMain, R.anim.out_from_right));
                            }
                            playModeFlipper.getInAnimation().setAnimationListener(new Animation.AnimationListener() {
                                public void onAnimationStart(Animation animation) {
                                }

                                public void onAnimationRepeat(Animation animation) {
                                }

                                public void onAnimationEnd(Animation animation) {
                                    startNextTurn2();
                                }
                            });
                            playModeFlipper.setDisplayedChild(1);
                            mBottomNavigation.getMenu().getItem(1).setChecked(true);
                            mBottomNavigation.getMenu().getItem(0).setChecked(false);
                            mBottomNavigation.getMenu().getItem(2).setChecked(false);
                        } else {
                            startNextTurn2();
                        }
                    }
                });
            }
        }, 100, TimeUnit.MILLISECONDS);
    }

    public void startNextTurn2() {
        Boolean hitStatusPlayer = mGameModel.getPlayerGameBoard().getShipSlotAtIndex(opponentChosenTarget).isOccupied();
        String msg = getColoredSpanned(getResources().getString(R.string.play_mode_turn_chat) + turnCount, "#349edb");
        messages.append(msg + getResources().getString(R.string.play_mode_break_tags_chat));
        String msg2 = getColoredSpanned(getResources().getString(R.string.play_mode_divider_chat), "#349edb");
        messages.append(msg2 + getResources().getString(R.string.play_mode_break_tags_chat));
        if (hitStatusPlayer) {
            String msg3 = getColoredSpanned(getResources().getString(R.string.play_mode_opponent_hit_chat), "#db756b");
            messages.append(msg3 + getResources().getString(R.string.play_mode_break_tags_chat));
        } else {
            String msg3 = getColoredSpanned(getResources().getString(R.string.play_mode_opponent_hit_chat), "#db756b");
            messages.append(msg3 + getResources().getString(R.string.play_mode_break_tags_chat));
        }
        Boolean hitStatusOpponent = mGameModel.getOpponentGameBoard().getShipSlotAtIndex(playerChosenTarget).isOccupied();

        if (hitStatusOpponent) {
            String msg3 = getColoredSpanned(getResources().getString(R.string.play_mode_player_hit_chat), "#db756b");
            messages.append(msg3 + getResources().getString(R.string.play_mode_break_tags_chat));
        } else {
            String msg3 = getColoredSpanned(getResources().getString(R.string.play_mode_player_miss_chat), "#db756b");
            messages.append(msg3 + getResources().getString(R.string.play_mode_break_tags_chat));
        }
        appendToChat();
        mPlayModeEditTextSend.setEnabled(true);
        reinitializeUI();
        animating = false;
        fireButtonPressed = false;
        if (!gameOver) {
            turnCount++;
            playerTurnDone = false;
            opponentTurnDone = false;
        } else {
            String msg4 = getColoredSpanned(getResources().getString(R.string.play_mode_divider_chat), "#164077");
            messages.append(msg4 + getResources().getString(R.string.play_mode_break_tags_chat));
            String msg5;
            if (gameOverStatus == quickShipActivityMain.DRAW) {
                msg5 = getColoredSpanned(getResources().getString(R.string.play_mode_game_draw_chat), "#164077");
            } else if (gameOverStatus == quickShipActivityMain.WON) {
                msg5 = getColoredSpanned(getResources().getString(R.string.play_mode_game_won_chat), "#164077");
            } else {
                msg5 = getColoredSpanned(getResources().getString(R.string.play_mode_game_lost_chat), "#164077");
            }
            messages.append(msg5 + getResources().getString(R.string.play_mode_break_tags_chat));
            appendToChat();
            mPlayModeFireBtn.setBackgroundResource(R.drawable.firebutton_01);
            mPlayModeFireBtn.setText(getResources().getString(R.string.game_over_play_again_btn));
            mPlayModeFireBtn.setEnabled(true);
        }
    }

    public void debugView(View v) {
        setContentView(R.layout.debug_animation_screen);
        animating = false;
        gameOver = true;
        gameOverStatus = quickShipActivityMain.WON;
        FrameLayout debug_screen = (FrameLayout) findViewById(R.id.debug_animation_root);
        debugQuickShipViewPlayModeOpponentGrid testGrid = new debugQuickShipViewPlayModeOpponentGrid(this, mGameModel);
        debug_screen.addView(testGrid);
        FrameLayout debug_border_frame = (FrameLayout) findViewById(R.id.debug_top_frame_border);
        debug_border_frame.addView(new quickShipViewGridBorder(this, ContextCompat.getColor(this, R.color.play_mode_opponent_frame_color)));
        mFPSTextureView = (FPSTextureView) findViewById(R.id.animation_texture_view2);
    }

    public void debugStartAnimationBtn(View v) {
        if (animating) {
            animating = false;
            mFPSTextureView.removeAllChildren();
            mFPSTextureView.tickStop();
        } else {
            animating = true;
            mFPSTextureView.tickStart();
            startGameOverAnimation();
        }
    }

    public void pauseAnimation() {
        mFPSTextureView.tickStop();
    }

    private void createMissRocketBitmap(final float[] slotIndex, int currentIndex) {
        final DisplayObject bitmapDisplay = new DisplayObject();

        int rocketOriginX = randInt(0, Math.round(screenWidth));
        int rocketOriginY;
        float rocketAngle;
        float rocketX;
        float rocketY;
        int screenWidthQuads = Math.round(screenWidth / 4);

        currentIndex = currentIndex / 10;
        int yIndex = currentIndex % 10;
        if (yIndex <= 2) {
            rocketOriginY = Math.round(screenWidth);
            rocketY = slotIndex[1] + ((slotIndex[3] - slotIndex[1]) / 2);
            if (rocketOriginX < screenWidthQuads) {
                rocketX = slotIndex[0] - ((slotIndex[2] - slotIndex[0]) / 2);
            } else if (rocketOriginX < screenWidthQuads * 2) {
                rocketX = slotIndex[0] - ((slotIndex[2] - slotIndex[0]) / 4);
            } else if (rocketOriginX < screenWidthQuads * 3) {
                rocketX = slotIndex[0];
            } else {
                rocketX = slotIndex[0] + ((slotIndex[2] - slotIndex[0]) / 2);
            }
        } else {
            rocketOriginY = 0;
            rocketY = slotIndex[1] - (slotIndex[3] - slotIndex[1]);
            if (rocketOriginX < screenWidthQuads) {
                rocketX = slotIndex[0] - ((slotIndex[2] - slotIndex[0]) / 2);
            } else if (rocketOriginX < screenWidthQuads * 2) {
                rocketX = slotIndex[0] - ((slotIndex[2] - slotIndex[0]) / 4);
            } else if (rocketOriginX < screenWidthQuads * 3) {
                rocketX = slotIndex[0];
            } else {
                rocketX = slotIndex[0] + ((slotIndex[2] - slotIndex[0]) / 2);
            }
        }

        rocketAngle = getAngle(rocketOriginX, rocketOriginY, rocketX, rocketY);

        Bitmap rocket = scaleDownDrawableImage(R.drawable.sheet_rocket, Math.round(mCellWidth), Math.round(mCellWidth) * 2);

        SpriteSheetDrawer spriteSheetDrawer = new SpriteSheetDrawer(
                rocket,
                rocket.getWidth() / 2,
                rocket.getHeight(), 2)
                .frequency(2)
                .spriteLoop(true).rotateRegistration(rocket.getWidth() / 4, rocket.getHeight() / 2);

        bitmapDisplay.with(spriteSheetDrawer)
                .tween()
                .tweenLoop(false)
                .transform(rocketOriginX, rocketOriginY, 255, 1f, 1f, rocketAngle)
                .to(500, rocketX, rocketY, 255, 1f, 1f, rocketAngle, Ease.SINE_IN_OUT)
                .waitTime(100)
                .call(new AnimCallBack() {
                    @Override
                    public void call() {
                        createMissTextBitmap(slotIndex);
                        mFPSTextureView.removeChild(bitmapDisplay);
                    }
                })
                .end();

        mFPSTextureView.addChild(bitmapDisplay);
    }

    private void createHitRocketBitmap(final float[] slotIndex, int currentIndex, final Bitmap emoji) {
        final DisplayObject bitmapDisplay = new DisplayObject();

        int rocketOriginX = randInt(0, Math.round(screenWidth));
        int rocketOriginY;
        float rocketAngle;
        float rocketX;
        float rocketY;
        int screenWidthQuads = Math.round(screenWidth / 4);

        currentIndex = currentIndex / 10;
        int yIndex = currentIndex % 10;
        if (yIndex <= 2) {
            rocketOriginY = Math.round(screenWidth);
            rocketY = slotIndex[1] + ((slotIndex[3] - slotIndex[1]) / 2);
            if (rocketOriginX < screenWidthQuads) {
                rocketX = slotIndex[0] - ((slotIndex[2] - slotIndex[0]) / 2);
            } else if (rocketOriginX < screenWidthQuads * 2) {
                rocketX = slotIndex[0] - ((slotIndex[2] - slotIndex[0]) / 4);
            } else if (rocketOriginX < screenWidthQuads * 3) {
                rocketX = slotIndex[0];
            } else {
                rocketX = slotIndex[0] + ((slotIndex[2] - slotIndex[0]) / 2);
            }
        } else {
            rocketOriginY = 0;
            rocketY = slotIndex[1] - (slotIndex[3] - slotIndex[1]);
            if (rocketOriginX < screenWidthQuads) {
                rocketX = slotIndex[0] - ((slotIndex[2] - slotIndex[0]) / 2);
            } else if (rocketOriginX < screenWidthQuads * 2) {
                rocketX = slotIndex[0] - ((slotIndex[2] - slotIndex[0]) / 4);
            } else if (rocketOriginX < screenWidthQuads * 3) {
                rocketX = slotIndex[0];
            } else {
                rocketX = slotIndex[0] + ((slotIndex[2] - slotIndex[0]) / 2);
            }
        }

        rocketAngle = getAngle(rocketOriginX, rocketOriginY, rocketX, rocketY);

        Bitmap rocket = scaleDownDrawableImage(R.drawable.sheet_rocket, Math.round(mCellWidth), Math.round(mCellWidth) * 2);

        SpriteSheetDrawer spriteSheetDrawer = new SpriteSheetDrawer(
                rocket,
                rocket.getWidth() / 2,
                rocket.getHeight(), 2)
                .frequency(2)
                .spriteLoop(true).rotateRegistration(rocket.getWidth() / 4, rocket.getHeight() / 2);

        bitmapDisplay.with(spriteSheetDrawer)
                .tween()
                .tweenLoop(false)
                .transform(rocketOriginX, rocketOriginY, 255, 1f, 1f, rocketAngle)
                .to(500, rocketX, rocketY, 255, 1f, 1f, rocketAngle, Ease.SINE_IN_OUT)
                .waitTime(100)
                .call(new AnimCallBack() {
                    @Override
                    public void call() {
                        createHitTextBitmap(slotIndex, emoji);
                        mFPSTextureView.removeChild(bitmapDisplay);
                    }
                })
                .end();

        mFPSTextureView.addChild(bitmapDisplay);
    }

    private void createHitTextBitmap(final float[] slotIndex, final Bitmap emoji) {
        final DisplayObject bitmapDisplay = new DisplayObject();

        float bitmapX = slotIndex[0] - ((slotIndex[2] - slotIndex[0]) / 4);
        float bitmapY = slotIndex[1] - ((slotIndex[3] - slotIndex[1]) / 4);

        bitmapDisplay.with(new BitmapDrawer(mHitText).scaleRegistration(mHitText.getWidth() / 2, mHitText.getHeight() / 2))
                .tween()
                .tweenLoop(false)
                .transform(bitmapX, bitmapY)
                .to(500, bitmapX, bitmapY, 0, 6f, 6f, 0, Ease.SINE_IN_OUT)
                .waitTime(100)
                .call(new AnimCallBack() {
                    @Override
                    public void call() {
                        switch (animationStage) {
                            case quickShipActivityMain.ANIMSTAGE1:
                                if (animateFirst) {
                                    refreshPlayerBoard();
                                } else {
                                    refreshOpponentBoard();
                                }
                                break;
                            case quickShipActivityMain.ANIMSTAGE2:
                                if (animateFirst) {
                                    refreshOpponentBoard();
                                } else {
                                    refreshPlayerBoard();
                                }
                                break;
                        }
                    }
                })
                .waitTime(100)
                .call(new AnimCallBack() {
                    @Override
                    public void call() {
                        spawnRandomEmojis(emoji, slotIndex);
                    }
                })
                .transform(bitmapX, bitmapY, Util.convertAlphaFloatToInt(1f), 1f, 1f, 0)
                .call(new AnimCallBack() {
                    @Override
                    public void call() {
                        mFPSTextureView.removeChild(bitmapDisplay);
                    }
                })
                .end();

        mFPSTextureView.addChild(bitmapDisplay);
    }

    private void createMissTextBitmap(final float[] slotIndex) {
        final DisplayObject bitmapDisplay = new DisplayObject();

        float bitmapX = slotIndex[0] - ((slotIndex[2] - slotIndex[0]) / 4);
        float bitmapY = slotIndex[1] - ((slotIndex[3] - slotIndex[1]) / 4);

        bitmapDisplay.with(new BitmapDrawer(mMissText).scaleRegistration(mMissText.getWidth() / 2, mMissText.getHeight() / 2))
                .tween()
                .tweenLoop(false)
                .transform(bitmapX, bitmapY)
                .to(500, bitmapX, bitmapY, 0, 6f, 6f, 0, Ease.SINE_IN_OUT)
                .waitTime(100)
                .call(new AnimCallBack() {
                    @Override
                    public void call() {
                        switch (animationStage) {
                            case quickShipActivityMain.ANIMSTAGE1:
                                if (animateFirst) {
                                    refreshPlayerBoard();
                                } else {
                                    refreshOpponentBoard();
                                }
                                break;
                            case quickShipActivityMain.ANIMSTAGE2:
                                if (animateFirst) {
                                    refreshOpponentBoard();
                                } else {
                                    refreshPlayerBoard();
                                }
                                break;
                        }
                    }
                })
                .waitTime(1000)
                .call(new AnimCallBack() {
                    @Override
                    public void call() {
                        animationStage++;
                        nextAnimation();
                        mFPSTextureView.removeChild(bitmapDisplay);
                    }
                })
                .end();

        mFPSTextureView.addChild(bitmapDisplay);
    }

    public void spawnRandomEmojis(final Bitmap mBitmap, final float[] slotIndex) {
        Timer mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            long t0 = System.currentTimeMillis();

            @Override
            public void run() {
                // How long the explosion last for, ie. change to "System.currentTimeMillis() - t0 > 4000"
                // to make it last for 4 seconds
                if (System.currentTimeMillis() - t0 > 1000) {
                    cancel();
                    animationStage++;
                    nextAnimation();
                } else {
                    int randomAmount = randInt(2, 4);
                    for (int i = 0; i < randomAmount; i++) {
                        animateRandomEmoji(mBitmap, slotIndex);
                    }
                }
            }
        }, 0, 300);
    }

    public void animateRandomEmoji(Bitmap mBitmap, final float[] slotIndex) {
        final DisplayObject bitmapDisplay = new DisplayObject();

        float initialRotate = (float) randInt(0, 360);

        BitmapDrawer bitmapDrawer = new BitmapDrawer(mBitmap)
                .scaleRegistration(mBitmap.getWidth() / 2, mBitmap.getHeight() / 2)
                .rotateRegistration(0, 0);

        bitmapDisplay.with(bitmapDrawer)
                .tween()
                .tweenLoop(false)
                .transform(slotIndex[0] + ((slotIndex[2] - slotIndex[0]) / 4), slotIndex[1] + ((slotIndex[3] - slotIndex[1]) / 4), Util.convertAlphaFloatToInt(1f), 1f, 1f, initialRotate)
                .to(500, slotIndex[0] + ((slotIndex[2] - slotIndex[0]) / 4), slotIndex[1] + ((slotIndex[3] - slotIndex[1]) / 4), 0, 5f, 5f, initialRotate, Ease.SINE_IN_OUT)
                .waitTime(400)
                .transform(slotIndex[0] + ((slotIndex[2] - slotIndex[0]) / 4), slotIndex[1] + ((slotIndex[3] - slotIndex[1]) / 4), Util.convertAlphaFloatToInt(1f), 1f, 1f, initialRotate)
                .call(new AnimCallBack() {
                    @Override
                    public void call() {
                        mFPSTextureView.removeChild(bitmapDisplay);
                    }
                })
                .end();

        mFPSTextureView.addChild(bitmapDisplay);
    }

    private void createNewTurnMsgBitmap() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (playModeFlipper.getDisplayedChild() == 0 || playModeFlipper.getDisplayedChild() == 2) {
                            if (playModeFlipper.getDisplayedChild() == 0) {
                                playModeFlipper.setInAnimation(AnimationUtils.loadAnimation(mActivityMain, R.anim.in_from_right));
                                playModeFlipper.setOutAnimation(AnimationUtils.loadAnimation(mActivityMain, R.anim.out_from_left));
                            } else {
                                playModeFlipper.setInAnimation(AnimationUtils.loadAnimation(mActivityMain, R.anim.in_from_left));
                                playModeFlipper.setOutAnimation(AnimationUtils.loadAnimation(mActivityMain, R.anim.out_from_right));
                            }
                            playModeFlipper.getInAnimation().setAnimationListener(new Animation.AnimationListener() {
                                public void onAnimationStart(Animation animation) {
                                }

                                public void onAnimationRepeat(Animation animation) {
                                }

                                public void onAnimationEnd(Animation animation) {
                                    createNewTurnMsgBitmap2();
                                }
                            });
                            playModeFlipper.setDisplayedChild(1);
                            mBottomNavigation.getMenu().getItem(1).setChecked(true);
                            mBottomNavigation.getMenu().getItem(0).setChecked(false);
                            mBottomNavigation.getMenu().getItem(2).setChecked(false);
                        } else {
                            createNewTurnMsgBitmap2();
                        }
                    }
                });
            }
        }, 1000, TimeUnit.MILLISECONDS);
    }

    public void createNewTurnMsgBitmap2() {
        final DisplayObject bitmapDisplay = new DisplayObject();

        TextPaint textBoundPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        textBoundPaint.setStyle(Paint.Style.FILL);
        textBoundPaint.setColor(ContextCompat.getColor(mActivityMain, R.color.play_mode_new_turn1_msg_color));
        textBoundPaint.setTextAlign(Paint.Align.LEFT);

        Typeface custom_font = Typeface.createFromAsset(getAssets(), "badaboom.ttf");

        textBoundPaint.setTypeface(Typeface.create(custom_font, Typeface.NORMAL));

        textBoundPaint.setTextSize(Util.convertDpToPixel(60, this));

        String tweenTxt = getResources().getString(R.string.play_mode_new_turn_msg1);

        Rect bounds = new Rect();
        textBoundPaint.getTextBounds(tweenTxt, 0, tweenTxt.length(), bounds);

        float textWidth = bounds.width();
        float textHeight = bounds.height();

        TextDrawer textDrawer = new TextDrawer(tweenTxt, textBoundPaint)
                .rotateRegistration(textWidth / 2, textHeight / 2)
                .scaleRegistration(textWidth / 2, textHeight / 2);

        float bitmapX = (screenWidth / 2) - (textWidth / 2);
        //float bitmapY = screenWidth - (screenWidth / 1.4f);
        float bitmapY = screenWidth - (screenWidth / 2f);

        bitmapDisplay.with(textDrawer)
                .tween()
                .tweenLoop(false)
                .transform(0 - textWidth, bitmapY)
                .to(500, bitmapX, bitmapY, 255, 1f, 1f, 0, Ease.SINE_IN_OUT)
                .waitTime(900)
                .to(300, screenWidth, bitmapY, 255, 1f, 1f, 0, Ease.SINE_IN_OUT)
                .transform(0 - textWidth, bitmapY, 0, 1f, 1f, 0)
                .waitTime(700)
                .call(new AnimCallBack() {
                    @Override
                    public void call() {
                        animationStage++;
                        nextAnimation();
                        mFPSTextureView.removeChild(bitmapDisplay);
                    }
                })
                .end();

        mFPSTextureView.addChild(bitmapDisplay);
    }

    public void startGameOverAnimation() {
        final ArrayList<Integer> emojiList = generateEmojiArray(gameOverStatus);
        final Random randomGenerator = new Random();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                Timer mTimer = new Timer();
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (!gameOver) {
                            cancel();
                        } else {
                            for (int i = 0; i < 5; i++) {
                                int index = randomGenerator.nextInt(emojiList.size());
                                String randomEmoji = getEmojiByUnicode(emojiList.get(index));
                                spawnGameOverEmojis(randomEmoji);
                            }
                        }
                    }
                }, 0, 200);
                createGameOverText();
            }
        }, 800, TimeUnit.MILLISECONDS);
    }

    public void spawnGameOverEmojis(final String emoji) {
        Bitmap emojiBitmap = textToBitmap(emoji, mCellWidth);
        float randomStartingX = randInt(0, Math.round(screenWidth));
        int randomAccelerationX = randInt(0, 1);
        if (randomAccelerationX == 0) {
            randomAccelerationX = -8;
        } else {
            randomAccelerationX = 8;
        }
        int randomAccelerationY = randInt(0, 2);

        final DisplayObject bitmapDisplay = new DisplayObject();
        bitmapDisplay.with(new BitmapDrawer(emojiBitmap))
                .parabolic()
                .transform(randomStartingX, -mCellWidth)
                .reboundBottom(false)
                .accelerationX(randomAccelerationX)
                .initialVelocityY(randomAccelerationY)
                .bottomHitCallback(new AnimCallBack() {
                    @Override
                    public void call() {
                        mFPSTextureView.removeChild(bitmapDisplay);
                    }
                })
                .end();

        mFPSTextureView.addChild(bitmapDisplay);
    }

    public void createGameOverText() {
        final DisplayObject bitmapDisplay = new DisplayObject();

        TextPaint textBoundPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        textBoundPaint.setStyle(Paint.Style.FILL);
        textBoundPaint.setColor(ContextCompat.getColor(mActivityMain, R.color.play_mode_gameover_color));
        textBoundPaint.setTextAlign(Paint.Align.LEFT);

        Typeface custom_font = Typeface.createFromAsset(getAssets(), "badaboom.ttf");

        textBoundPaint.setTypeface(Typeface.create(custom_font, Typeface.NORMAL));

        textBoundPaint.setTextSize(Util.convertDpToPixel(60, this));

        String tweenTxt = "";
        switch (gameOverStatus) {
            case quickShipActivityMain.WON:
                tweenTxt = getResources().getString(R.string.play_mode_game_won);
                break;
            case quickShipActivityMain.LOST:
                tweenTxt = getResources().getString(R.string.play_mode_game_lost);
                break;
            case quickShipActivityMain.DRAW:
                tweenTxt = getResources().getString(R.string.play_mode_game_draw);
                break;
        }

        Rect bounds = new Rect();
        textBoundPaint.getTextBounds(tweenTxt, 0, tweenTxt.length(), bounds);

        float textWidth = bounds.width();
        float textHeight = bounds.height();

        TextDrawer textDrawer = new TextDrawer(tweenTxt, textBoundPaint)
                .rotateRegistration(textWidth / 2, textHeight / 2)
                .scaleRegistration(textWidth / 2, textHeight / 2);

        float bitmapX = (screenWidth / 2) - (textWidth / 2);
        float bitmapY = screenWidth - (screenWidth / 2f);

        bitmapDisplay.with(textDrawer)
                .tween()
                .tweenLoop(false)
                .transform(bitmapX, 0 - bitmapY)
                .to(600, bitmapX, bitmapY, 255, 1f, 1f, 0, Ease.SINE_IN_OUT)
                .end();

        mFPSTextureView.addChild(bitmapDisplay);
    }

    public static Bitmap textToBitmap(String text, float textWidth) {
        final float testTextSize = 48f;
        TextPaint textBoundPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        textBoundPaint.setStyle(Paint.Style.FILL);
        textBoundPaint.setColor(Color.BLACK);
        textBoundPaint.setTextAlign(Paint.Align.LEFT);

        textBoundPaint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        textBoundPaint.getTextBounds(text, 0, text.length(), bounds);

        float calculatedTextSize = (testTextSize * textWidth / bounds.width()) - 2;
        textBoundPaint.setTextSize(calculatedTextSize);

        StaticLayout mTextLayout = new StaticLayout(text, textBoundPaint, Math.round(textWidth), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

        Bitmap b = Bitmap.createBitmap(Math.round(textWidth), mTextLayout.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        c.save();
        c.translate(2, 0);
        mTextLayout.draw(c);
        c.restore();

        return b;
    }

    public void setHitText(Bitmap b) {
        mHitText = b;
    }

    public void setMissText(Bitmap b) {
        mMissText = b;
    }

    // Pick a random number from min to max (inclusive)
    public static int randInt(int min, int max) {

        Random rand = new Random();

        return rand.nextInt((max - min) + 1) + min;
    }

    public void setCellWidth(float cellWidth) {
        mCellWidth = cellWidth;
    }

    public float getAngle(float initialX, float initialY, float targetX, float targetY) {
        float angle = (float) Math.toDegrees(Math.atan2(targetY - initialY, targetX - initialX));

        if (angle < 0) {
            angle += 360;
        }

        return angle;
    }

    public void refreshPlayerBoard() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGameModel.getPlayerGameBoard().setHit(opponentChosenTarget, true);
                mGameModel.getPlayerGameBoard().getShipSlotAtIndex(opponentChosenTarget).setEmoji(opponentChosenEmoji);
                playModePlayerGrid.invalidate();
            }
        });
    }

    public void refreshOpponentBoard() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGameModel.getOpponentGameBoard().setHit(playerChosenTarget, true);
                mGameModel.getOpponentGameBoard().getShipSlotAtIndex(playerChosenTarget).setEmoji(playerChosenEmoji);
                playModeOpponentGrid.invalidate();
            }
        });
    }

    public boolean getAnimating() {
        return animating;
    }

    public boolean getGameOver() {
        return gameOver;
    }

    public String getEmojiByUnicode(int unicode) {
        return new String(Character.toChars(unicode));
    }

    public ArrayList<Integer> generateEmojiArray(int gameOverStatus) {
        ArrayList<Integer> returnArray = new ArrayList<>();
        switch (gameOverStatus) {
            case quickShipActivityMain.WON:
                returnArray.add(0x1f436);
                returnArray.add(0x1f43a);
                returnArray.add(0x1f431);
                returnArray.add(0x1f42d);
                returnArray.add(0x1f439);
                returnArray.add(0x1f430);
                returnArray.add(0x1f438);
                returnArray.add(0x1f42f);
                returnArray.add(0x1f428);
                returnArray.add(0x1f43b);
                returnArray.add(0x1f437);
                returnArray.add(0x1f43d);
                returnArray.add(0x1f42e);
                returnArray.add(0x1f417);
                returnArray.add(0x1f435);
                returnArray.add(0x1f412);
                returnArray.add(0x1f434);
                returnArray.add(0x1f411);
                returnArray.add(0x1f418);
                returnArray.add(0x1f43c);
                returnArray.add(0x1f427);
                returnArray.add(0x1f426);
                returnArray.add(0x1f424);
                returnArray.add(0x1f425);
                returnArray.add(0x1f423);
                returnArray.add(0x1f414);
                returnArray.add(0x1f40d);
                returnArray.add(0x1f422);
                returnArray.add(0x1f41b);
                returnArray.add(0x1f41d);
                returnArray.add(0x1f41c);
                returnArray.add(0x1f41e);
                returnArray.add(0x1f40c);
                returnArray.add(0x1f419);
                returnArray.add(0x1f41a);
                returnArray.add(0x1f420);
                returnArray.add(0x1f41f);
                returnArray.add(0x1f42c);
                returnArray.add(0x1f433);
                returnArray.add(0x1f40b);
                returnArray.add(0x1f404);
                returnArray.add(0x1f40f);
                returnArray.add(0x1f400);
                returnArray.add(0x1f403);
                returnArray.add(0x1f405);
                returnArray.add(0x1f407);
                returnArray.add(0x1f409);
                returnArray.add(0x1f40e);
                returnArray.add(0x1f410);
                returnArray.add(0x1f413);
                returnArray.add(0x1f415);
                returnArray.add(0x1f416);
                returnArray.add(0x1f401);
                returnArray.add(0x1f402);
                returnArray.add(0x1f432);
                returnArray.add(0x1f421);
                returnArray.add(0x1f40a);
                returnArray.add(0x1f42b);
                returnArray.add(0x1f42a);
                returnArray.add(0x1f406);
                returnArray.add(0x1f408);
                returnArray.add(0x1f429);
                returnArray.add(0x1f43e);
                returnArray.add(0x1f490);
                returnArray.add(0x1f338); //invis ?
                returnArray.add(0x1f337);
                returnArray.add(0x1f340);
                returnArray.add(0x1f339);
                returnArray.add(0x1f33b);
                returnArray.add(0x1f33a); //invis ?
                returnArray.add(0x1f341);
                returnArray.add(0x1f343);
                returnArray.add(0x1f342);
                returnArray.add(0x1f33f);
                returnArray.add(0x1f33e);
                returnArray.add(0x1f344);
                returnArray.add(0x1f335);
                returnArray.add(0x1f334);
                returnArray.add(0x1f332);
                returnArray.add(0x1f333);
                returnArray.add(0x1f330);
                returnArray.add(0x1f331);
                returnArray.add(0x1f33c);
                break;
            case quickShipActivityMain.LOST:
                returnArray.add(0x1f633);
                returnArray.add(0x1f614);
                returnArray.add(0x1f60c);
                returnArray.add(0x1f612);
                returnArray.add(0x1f61e);
                returnArray.add(0x1f623);
                returnArray.add(0x1f622);
                returnArray.add(0x1f602);
                returnArray.add(0x1f62d);
                returnArray.add(0x1f62a);
                returnArray.add(0x1f625);
                returnArray.add(0x1f630);
                returnArray.add(0x1f605);
                returnArray.add(0x1f613);
                returnArray.add(0x1f629);
                returnArray.add(0x1f62b);
                returnArray.add(0x1f628);
                returnArray.add(0x1f631);
                returnArray.add(0x1f620);
                returnArray.add(0x1f621);
                returnArray.add(0x1f624);
                returnArray.add(0x1f616);
                returnArray.add(0x1f606);
                returnArray.add(0x1f60b);
                returnArray.add(0x1f635);
                returnArray.add(0x1f632);
                returnArray.add(0x1f61f);
                returnArray.add(0x1f626);
                returnArray.add(0x1f627);
                returnArray.add(0x1f62f);
                returnArray.add(0x1f63f);
                returnArray.add(0x1f63e);
                returnArray.add(0x1f4a9);
                returnArray.add(0x1f44e);
                returnArray.add(0x1f494);
                returnArray.add(0x1f480);
                returnArray.add(0x1f4a7);
                break;
            case quickShipActivityMain.DRAW:
                returnArray.add(0x1f604);
                returnArray.add(0x1f603);
                returnArray.add(0x1f600);
                returnArray.add(0x1f60a);
                returnArray.add(0x263a);
                returnArray.add(0x1f609);
                returnArray.add(0x1f60d);
                returnArray.add(0x1f618);
                returnArray.add(0x1f61a);
                returnArray.add(0x1f617);
                returnArray.add(0x1f61c);
                returnArray.add(0x1f61d);
                returnArray.add(0x1f61b);
                returnArray.add(0x1f601);
                returnArray.add(0x1f60e);
                returnArray.add(0x1f63a);
                returnArray.add(0x1f638);
                returnArray.add(0x1f63b);
                returnArray.add(0x1f63d);
                returnArray.add(0x1f63c);
                returnArray.add(0x1f44d);
                returnArray.add(0x1f44c);
                returnArray.add(0x1f64c);
                returnArray.add(0x1f64f);
                returnArray.add(0x1f4aa);
                returnArray.add(0x1f645);
                returnArray.add(0x1f451);
                //returnArray.add(0x2764);
                returnArray.add(0x1f48e);
                returnArray.add(0x1f60f);
                returnArray.add(0x1f64b);
                break;
        }
        return returnArray;
    }

    public void appendToChat() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    mChooseModeChatMessageLog.setText(Html.fromHtml(messages.toString(), Html.FROM_HTML_MODE_LEGACY));
                    mPlayModeChatMessageLog.setText(Html.fromHtml(messages.toString(), Html.FROM_HTML_MODE_LEGACY));
                } else {
                    mChooseModeChatMessageLog.setText(Html.fromHtml(messages.toString()));
                    mPlayModeChatMessageLog.setText(Html.fromHtml(messages.toString()));
                }
                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

                scheduler.schedule(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mChooseModeScroller.smoothScrollTo(0, mChooseModeChatMessageLog.getBottom());
                                mPlayModeScroller.smoothScrollTo(0, mPlayModeChatMessageLog.getBottom());
                            }
                        });
                    }
                }, 400, TimeUnit.MILLISECONDS);
            }
        });
    }

    public boolean getFireButtonPressed() {
        return fireButtonPressed;
    }
}