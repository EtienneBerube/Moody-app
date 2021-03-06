package ultramirinc.champs_mood;
//Done
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import ultramirinc.champs_mood.managers.UserManager;
import ultramirinc.champs_mood.models.Break;
import ultramirinc.champs_mood.models.User;

/**
 * Created by Etienne Berube on 2017-05-05.
 * This is the profile of a given user (That is the the current user). This allows the user to see
 * his friend's profile and location
 */


public class FriendProfileActivity extends AppCompatActivity implements OnMapReadyCallback {
    /**Contains the map used*/
    private GoogleMap mMap;
    /**Contains the friend's User object*/
    private User friendProfile;
    /**Contains the friend's location*/
    private LatLng latLng;
    /**Contains a progress dialog*/
    private ProgressDialog progressDialog;

    public FriendProfileActivity(){
    }
    /**Creates and inflates the layout of the activity.*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendprofil);

        Intent intent = getIntent();

        progressDialog = new ProgressDialog(this);

        try {
            String userId = intent.getExtras().get("userId").toString();
            loadFriendProfile(userId);
        }
        catch (Exception e) {
            //error occured, do nothing.
        }

        Button button = (Button) findViewById(R.id.friendShipButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (button.getText().toString().equals("Follow")) {
                    follow(friendProfile);
                }
                else {
                    unFollow(friendProfile);
                }
            }
        });

        SupportMapFragment mMap = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMap.getMapAsync(this);
    }
    /**Gets the user's information from the database.*/
    public void loadFriendProfile(String userId) {
        progressDialog.setMessage("Loading user data...");
        progressDialog.show();
        DatabaseReference databaseUsers = FirebaseDatabase.getInstance().getReference("users");
        Query userQuery = databaseUsers.orderByChild("id").equalTo(userId);
        ValueEventListener loadInfoListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    friendProfile = singleSnapshot.getValue(User.class);
                }

                latLng = new LatLng(friendProfile.getLastLocation().getLat(),
                        friendProfile.getLastLocation().getLng());

                if(friendProfile.isLocationShared()) {
                    Log.d("Location Debug: ", latLng.toString());
                    mMap.addMarker(new MarkerOptions().position(latLng).title(friendProfile.getName()));
                }

                TextView name = (TextView) findViewById(R.id.profil_text);
                name.setText(friendProfile.getName());

                TextView mood = (TextView) findViewById(R.id.mood);

                if(friendProfile.getMood() == null) {
                    mood.setText("No mood");
                }else if( friendProfile.getMood().equals("")){
                    mood.setText("No Mood");
                }else {
                    mood.setText(friendProfile.getMood());
                }

                TextView breakText = (TextView) findViewById(R.id.breakText);
                breakText.setText(friendProfile.getBreakText());

                checkBreakStatus();
                updateFriendShipButton(UserManager.getInstance().getCurrentUser().isFriend(friendProfile));

                try {
                    TextView floor = (TextView) findViewById(R.id.floorLevel);

                    if (friendProfile.isLocationShared()) {
                        floor.setText(Integer.toString(friendProfile.getFloor()));
                    }
                    else {
                        floor.setText("Floor not shared");
                    }
                }catch (Exception e) {
                    //empty
                }
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                }, 500);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        userQuery.addListenerForSingleValueEvent(loadInfoListener);
    }
    /**Allows to unfollow the given user.*/
    private boolean unFollow(User userToUnfollow) {
        boolean done = UserManager.getInstance().getCurrentUser().removeFromFriendList(userToUnfollow);
        //save
        if (done) {
            UserManager.getInstance().editUserInformations(UserManager.getInstance().getCurrentUser());
            updateFriendShipButton(false);
        }
        Toast.makeText(this, "Unfollowed", Toast.LENGTH_SHORT).show();
        return done;
    }
    /**Allows to follow the given user.*/
    private void follow(User userToFollow) {
        boolean done = UserManager.getInstance().getCurrentUser().addToFriendList(userToFollow);
        //save
        if (done) {
            UserManager.getInstance().editUserInformations(UserManager.getInstance().getCurrentUser());
            updateFriendShipButton(true);
        }
        Toast.makeText(this, "Friend request sent!", Toast.LENGTH_SHORT).show();
    }
    /**Updates the text of the button.*/
    public void updateFriendShipButton(boolean isFriend) {
        Button button = (Button) findViewById(R.id.friendShipButton);

        String textButton = isFriend ? "Unfollow" : "Follow";
        button.setText(textButton);
    }
    /**Checks the status of the friend's break (in break in X minutes, in break or no more breaks).*/
    private void checkBreakStatus() {

        ArrayList<Break> friendBreaks = new ArrayList<>();
        //Loading things from db
        DatabaseReference breaksReference = FirebaseDatabase.getInstance().getReference("breaks");
        Query breakQuery = breaksReference.orderByChild("userId").equalTo(friendProfile.getId());
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    friendBreaks.add(singleSnapshot.getValue(Break.class));
                }
                java.util.GregorianCalendar current = new java.util.GregorianCalendar();
                int currentDay = adaptDayOfWeek(current.get(java.util.Calendar.DAY_OF_WEEK));

                long closestBreakMin = 1440;
                Break closestBreak = null;
                long closestBreakDuration = 0;

                // find closest break for the day (if any)
                for (int i=0; i < friendBreaks.size(); i++) {
                    Break breakNode = friendBreaks.get(i);
                    if (currentDay == breakNode.getIntDay()) {
                        java.util.GregorianCalendar now = new java.util.GregorianCalendar();

                        java.util.GregorianCalendar breakStart = new java.util.GregorianCalendar();
                        breakStart.set(java.util.Calendar.HOUR_OF_DAY,breakNode.getStart().getHour());
                        breakStart.set(java.util.Calendar.MINUTE,(breakNode.getStart().getMinute()));
                        breakStart.set(java.util.Calendar.SECOND,0);

                        long timeDiffInMinutes = getDateDiff(now, breakStart, TimeUnit.MINUTES);

                        java.util.GregorianCalendar breakEnd = new java.util.GregorianCalendar();
                        breakEnd.set(java.util.Calendar.HOUR_OF_DAY,breakNode.getEnd().getHour());
                        breakEnd.set(java.util.Calendar.MINUTE,breakNode.getEnd().getMinute());
                        breakEnd.set(java.util.Calendar.SECOND,0);


                        //check if currently in break.
                        long breakDuration = getDateDiff(breakStart, breakEnd, TimeUnit.MINUTES);
                        if (timeDiffInMinutes < 0 && Math.abs(timeDiffInMinutes) <= breakDuration) {
                            //is currently in break! // Loop stops here.
                            closestBreak = breakNode;
                            closestBreakMin = timeDiffInMinutes;
                            closestBreakDuration = breakDuration;
                            break;
                        } else if (timeDiffInMinutes < closestBreakMin) {
                            //this is to handle if there are multiple breaks in one day. The goal is to show when is the CLOSEST break.
                            closestBreak = breakNode;
                            closestBreakMin = timeDiffInMinutes;
                            closestBreakDuration = breakDuration;
                        }
                        else if (timeDiffInMinutes > 0 && closestBreakMin < 0) {
                            //There is still a break in the current day, while the closest break is already passed, so the coming break has priority. (get it?)
                            closestBreak = breakNode;
                            closestBreakMin = timeDiffInMinutes;
                            closestBreakDuration = breakDuration;
                        }
                    }
                }
                String text = "";
                if (closestBreak == null) {
                    TextView tv = (TextView) findViewById(R.id.breakText);
                    if (tv != null) {
                        tv.setText("No breaks today");
                    }
                }
                else {
                    if (closestBreakMin > 0) {
                        // Next break in timediffminutes
                        text = "Next break at : " + closestBreak.getFromTime();
                    }
                    else if (closestBreakMin < 0 && Math.abs(closestBreakMin) <= closestBreakDuration) {
                        text = "In break until : " + closestBreak.getToTime();
                    }
                    else {
                        // break is over.
                        text = "No more breaks today";
                    }


                    TextView tv = (TextView) findViewById(R.id.breakText);
                    if (tv != null) {
                        tv.setText(text);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        breakQuery.addListenerForSingleValueEvent(postListener);
    }
    /**There is a mismatch between our original int values of weekdays with the java.utils.calendar int values of weekdays.*/
    private int adaptDayOfWeek(int weekday) {
        int newValue = 0;
        switch(weekday) {
            case 1:
                newValue = 7;
                break;
            case 2:
                newValue = 1;
                break;
            case 3:
                newValue = 2;
                break;
            case 4:
                newValue = 3;
                break;
            case 5:
                newValue = 4;
                break;
            case 6:
                newValue = 5;
                break;
            case 7:
                newValue = 6;
                break;
        }
        return newValue;
    }
    /**Returns the time difference in milliseconds between two dates.*/
    public static long getDateDiff(java.util.GregorianCalendar date1, java.util.GregorianCalendar date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime().getTime() - date1.getTime().getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }
    /**Sets the map when it is ready (Location, zoom, position, markers, etc.).*/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("debug", "in");
        mMap = googleMap;
        mMap.clear();

        // Add a marker in Sydney and move the camera
        LatLng champlain = new LatLng(45.5164522,-73.52062409999996);


        CameraPosition pos = CameraPosition.builder().target(champlain).bearing(-103).build(); //rotate map
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));


        mMap.setMinZoomPreference((float)17.3);
        mMap.setMaxZoomPreference(20);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
    }
    /**Handles the event when the back button is pressed on the android device.*/
    @Override
    public void onBackPressed(){
        finish();
    }
}
