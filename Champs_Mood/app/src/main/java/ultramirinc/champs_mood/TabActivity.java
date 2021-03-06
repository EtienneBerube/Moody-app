package ultramirinc.champs_mood;
//Done
import android.support.v4.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import ultramirinc.champs_mood.fragments.FriendsFragment;
import ultramirinc.champs_mood.fragments.HomeFragment;
import ultramirinc.champs_mood.fragments.NotificationFragment;
import ultramirinc.champs_mood.fragments.ProfileFragment;
import ultramirinc.champs_mood.fragments.SearchFragment;
import ultramirinc.champs_mood.managers.UserManager;
import ultramirinc.champs_mood.models.User;


/**
 * Created by Etienne Berube on 2017-03-23.
 * This class is the mainframe of the application. Every function or fragment is link to this class. Here is the heart
 * of the app.
 */
public class TabActivity extends AppCompatActivity {

    /**Contains the view pager for the activity*/
    private MyViewPager mViewPager;
    /**Contains a section pager adapter for the activity*/
    private SectionsPagerAdapter mSectionsPagerAdapter;
    /**Contains a progress dialog*/
    private ProgressDialog progressDialog;

    public TabActivity(){
    }
    /**Creates and inflate the visual layout for the Activity.*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        Intent intent = getIntent();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Moods");
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        progressDialog = new ProgressDialog(this);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (MyViewPager) findViewById(R.id.container);
        mViewPager.setPagingEnabled(false);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        loadUserInformationSetupViewPager(mViewPager);

    }
    /**Gets user's information from the database and sets up the ViewPager.*/
    private void loadUserInformationSetupViewPager(MyViewPager viewPager) {

            progressDialog.setMessage("Loading user data...");
            progressDialog.show();
            DatabaseReference databaseUsers = FirebaseDatabase.getInstance().getReference("users");
            Query userQuery = databaseUsers.orderByChild("id").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = null;
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        removeKeyboard();
                        user = singleSnapshot.getValue(User.class);
                        UserManager.getInstance().setCurrentUser(user);
                        setupViewPager(viewPager);

                        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
                        tabLayout.addTab(tabLayout.newTab().setText("Home"));
                        tabLayout.addTab(tabLayout.newTab().setText("Friends"));
                        tabLayout.addTab(tabLayout.newTab().setText("Search"));
                        tabLayout.addTab(tabLayout.newTab().setText("Notif"));
                        tabLayout.addTab(tabLayout.newTab().setText("Profile"));

                        tabLayout.setupWithViewPager(mViewPager);

                        tabLayout.getTabAt(0).setIcon(R.drawable.ic_tab_home);
                        tabLayout.getTabAt(1).setIcon(R.drawable.ic_action_firends);
                        tabLayout.getTabAt(2).setIcon(R.drawable.ic_action_search);
                        tabLayout.getTabAt(3).setIcon(R.drawable.ic_action_notif);
                        tabLayout.getTabAt(4).setIcon(R.drawable.ic_action_profil);
                        progressDialog.dismiss();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            userQuery.addListenerForSingleValueEvent(postListener);
    }
    /**Sets up the ViewPager.*/
    private void setupViewPager(MyViewPager viewPager){
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.addFragment(new HomeFragment());
        mSectionsPagerAdapter.addFragment(new FriendsFragment());
        mSectionsPagerAdapter.addFragment(new SearchFragment());
        mSectionsPagerAdapter.addFragment(new NotificationFragment());
        mSectionsPagerAdapter.addFragment(new ProfileFragment());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Nothing
            }

            @Override
            public void onPageSelected(int position) {
                String message = "";

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                switch (position){

                    case 0: message = "Moods";
                        //ft.replace(R.id.container, new HomeFragment()); break;
                        getSupportFragmentManager()
                                .beginTransaction()
                                .detach(((SectionsPagerAdapter)(mViewPager.getAdapter())).getItem(position))
                                .attach(((SectionsPagerAdapter)(mViewPager.getAdapter())).getItem(position))
                                .commit();
                        break;

                    case 1: message = "Friends";
                        //ft.replace(R.id.container, new FriendsFragment()); break;
                        getSupportFragmentManager()
                                .beginTransaction()
                                .detach(((SectionsPagerAdapter)(mViewPager.getAdapter())).getItem(position))
                                .attach(((SectionsPagerAdapter)(mViewPager.getAdapter())).getItem(position))
                                .commit();
                        break;

                    case 2: message = "Search";
                        //ft.replace(R.id.container, new SearchFragment()); break;
                        getSupportFragmentManager()
                                .beginTransaction()
                                .detach(((SectionsPagerAdapter)(mViewPager.getAdapter())).getItem(position))
                                .attach(((SectionsPagerAdapter)(mViewPager.getAdapter())).getItem(position))
                                .commit();
                        break;

                    case 3: message = "Notifications";
                        //ft.replace(R.id.container, new NotificationFragment());break;
                        getSupportFragmentManager()
                                .beginTransaction()
                                .detach(((SectionsPagerAdapter)(mViewPager.getAdapter())).getItem(position))
                                .attach(((SectionsPagerAdapter)(mViewPager.getAdapter())).getItem(position))
                                .commit();
                        break;

                    case 4: message = "Profile";
                        //ft.replace(R.id.container, new ProfileFragment());break;
                        getSupportFragmentManager()
                                .beginTransaction()
                                .detach(((SectionsPagerAdapter)(mViewPager.getAdapter())).getItem(position))
                                .attach(((SectionsPagerAdapter)(mViewPager.getAdapter())).getItem(position))
                                .commit();
                        break;

                    default: message = "Default"; break;
                }
                getSupportActionBar().setTitle(message);
                //ft.commit();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //Nothing
            }
        });
    }
    /**Inflates the toolbar with a given XML file.*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tab, menu);
        return true;
    }
    /**Handles the actions when a icon in the toolbar is clicked.*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;


            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
    /**Removes the keyboard from the screen.*/
    public void removeKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            view.clearFocus();
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    /**Removes the progress dialog when the activity is paused.*/
    @Override
    public void onPause(){
        super.onPause();
        progressDialog.dismiss();
    }

    /**Quits the application when the back button of the android device is pressed.*/
    @Override
    public void onBackPressed(){
        finish();
    }



    /**
     * A placeholder fragment containing a simple view.
     */


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        /**Contains a list of all fragments of the TabActivity*/
        private final List<Fragment> mFragmentList = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**Adds a fragment to the list of fragments.*/
        public void addFragment(Fragment fragment) {
            mFragmentList.add(fragment);
        }
        /**Gets a Fragment from the list a given position.*/
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }
        /**Gets the number of fragments in the list.*/
        @Override
        public int getCount() {
            return mFragmentList.size();
        }


    }
}
