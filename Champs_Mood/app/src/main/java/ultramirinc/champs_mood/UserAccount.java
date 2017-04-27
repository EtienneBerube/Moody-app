package ultramirinc.champs_mood;

import java.util.Calendar;
import android.location.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import java.util.GregorianCalendar;
import java.util.Iterator;

import java.util.List;

/**
 * Created by Étienne Bérubé on 2017-02-07.
 */

public class UserAccount {
    public static final int IN_BREAK = 0;
    public static final int NOT_IN_BREAK = 1;


    private String id;
    private String name;
    private String mood;
    private String breakText; //TODO temporary
    private boolean isFriend; //TODO temporary
    private Collection<UserAccount> friendList = Collections.synchronizedList(new ArrayList<>());
    private ArrayList<Break> breaks = new ArrayList<Break>();
    private Location mLastLocation;
    private int floor;

    public UserAccount() {
        this.name = "DEFAULT";
        this.mood = "DEFAULT_MOOD";
    }

    public UserAccount(String name, String mood, String breakText, boolean isFriend) { //TODO temporary
        this.name = name;
        this.mood = mood;
        this.breakText = breakText;
        this.isFriend = isFriend;
    }

    public UserAccount(String id, String name, String mood, Location location) { //Possible change
        this.id = id;
        this.name = name;
        this.mood = mood;
        this.mLastLocation = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public void populateFriendList(ArrayList<UserAccount> friends){ //TODO update method
        for(UserAccount u: friends)
            this.friendList.add(u);
    }

    public void addToFriendList(UserAccount user){
        friendList.add(user);
    }

    public Collection<UserAccount> getFriendList() {
        return friendList;
    }


    public String getBreakStatus() {
        GregorianCalendar cal = new GregorianCalendar();
        int currentDay = cal.get(Calendar.DAY_OF_WEEK);
        String today;

        switch (currentDay) {
            case 2:
                today = "Monday";
                break;
            case 3:
                today = "Tuesday";
                break;
            case 4:
                today = "Wednesday";
                break;
            case 5:
                today = "Thursday";
                break;
            case 6:
                today = "Friday";
                break;
            default:
                today = "else";
        }


        return "";
    }

    public boolean isFriend(UserAccount user){

        Iterator<UserAccount> it = friendList.iterator();

        while(it.hasNext()){
            UserAccount friend = it.next();

            if(friend.getId().equals(user.getId())){
                return true;
            }
        }
        return false;
    }

    public String getFriendStatus(UserAccount user){
        if(isFriend(user)){
            return "Poke !";
        }
        else{
            return "Add";
        }
    }

    public String getFriendStatusTemp(){ //temporary
        if(isFriend){
            return "Poke !";
        }
        else{
            return "Add";
        }
    }

    public String getBreakTextTemp(){ //temporary
        return breakText;
    }

    public String getBreakText() {//TODO debug
        ArrayList<Break> temp = new ArrayList<>();

        Collections.sort(breaks);
        Iterator<Break> iterator = this.breaks.iterator();

        GregorianCalendar mCalendar = new GregorianCalendar();
        int dayInt = mCalendar.get(Calendar.DAY_OF_WEEK);
        int currentHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = mCalendar.get(Calendar.MINUTE);

        String dayName = "";

        switch (dayInt){
            case (1): dayName = "Monday"; break;
            case (2): dayName = "Tuesday"; break;
            case (3): dayName = "Wednesday"; break;
            case (4): dayName = "Thursday"; break;
            case (5): dayName = "Friday"; break;
            default: dayName = "Other";break;
        }

        //find subset of Elements with same day
        while(iterator.hasNext()){
            Break tempBreak = iterator.next();
            if(tempBreak.getDay().equals(dayName)){
                temp.add(tempBreak);
            }
        }

        for(int i = 0; i < temp.size(); i++){
            Break tempBreak = temp.get(i);

            if(currentHour < tempBreak.getStart().getHour()){
                    breakText = "Break in" + tempBreak.getTimeDifference() + " minutes";
                    break;
            }
            if (currentHour == tempBreak.getStart().getHour()){
                if(currentMinute < tempBreak.getStart().getMinute()){
                    breakText = "Break in" + tempBreak.getTimeDifference() + " minutes";
                    break;
                }else{
                    breakText = "In break";
                    break;
                }
            }
            if(currentHour > tempBreak.getStart().getHour() && currentHour < tempBreak.getEnd().getHour()){
                breakText = "In break";
                break;
            }

            if(currentHour == tempBreak.getEnd().getHour()){
                if(currentMinute < tempBreak.getEnd().getMinute()){
                    breakText = "In break";
                    break;
                }
            }
            if(i == (temp.size() - 1) && (((currentHour == tempBreak.getEnd().getHour())
                    && (currentMinute > tempBreak.getEnd().getMinute())) || (currentHour > tempBreak.getEnd().getHour()))){
                breakText = "No more breaks today";
                break;
            }
        }

        return breakText;
    }

    public ArrayList<Break> getBreaks() {
        return breaks;
    }

    public void setBreaks(ArrayList<Break> breaks) {
        this.breaks = breaks;

    }
}
