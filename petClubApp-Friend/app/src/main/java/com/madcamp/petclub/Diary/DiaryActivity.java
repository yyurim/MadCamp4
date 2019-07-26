package com.madcamp.petclub.Diary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.madcamp.petclub.Board.BoardActivity;
import com.madcamp.petclub.Diary.decorators.HighlightWeekendsDecorator;
import com.madcamp.petclub.Diary.decorators.MySelectorDecorator;
import com.madcamp.petclub.Diary.decorators.OneDayDecorator;
//import com.madcamp.petclub.Diary.decorators.SaturdayDecorator;
//import com.madcamp.petclub.Diary.decorators.SundayDecorator;
import com.madcamp.petclub.Diary.decorators.TodayDecorator;
import com.madcamp.petclub.Friends.SplashActivity;
import com.madcamp.petclub.Hospital.HospitalActivity;
import com.madcamp.petclub.MyInfo.MyInfoActivity;
import com.madcamp.petclub.MyPets.MyPetActivity;
import com.madcamp.petclub.News.NewsActivity;
import com.madcamp.petclub.R;
import com.madcamp.petclub.login.SharedPreference;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
//import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DiaryActivity extends AppCompatActivity
        implements OnDateSelectedListener, NavigationView.OnNavigationItemSelectedListener {

    public static ArrayList<DiaryClass> diary_data;
    String userID, petName;
    private DatabaseReference diaryDB, dateDB;
    StorageReference mStorage;
    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    private final TodayDecorator todayDecorator = new TodayDecorator();
    public static ArrayList<CalendarDay> dates;

    @BindView(R.id.calendarView)
    MaterialCalendarView widget;

    @Override protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        petName = intent.getStringExtra("petName");
        userID = SharedPreference.getAttribute(getApplicationContext(), "userID");
        mStorage = FirebaseStorage.getInstance().getReference();
        setContentView(R.layout.content_my_diary);
        ButterKnife.bind(this);
        diary_data = new ArrayList<DiaryClass>();
        dates = new ArrayList<CalendarDay>();

        dateDB = FirebaseDatabase.getInstance().getReference("/pet/"+userID+"/"+petName);
        System.out.println("petName is "+petName);
        String sort_column_name="date";
        Query sortbyDate = dateDB.orderByChild(sort_column_name);
        sortbyDate.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dates.clear();
                System.out.println("dates all cleared");
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String key = postSnapshot.getKey();
                    if (key.equals("petAge")||key.equals("petGender")||key.equals("petName")||key.equals("petSpecies")||
                            key.equals("petBff")||key.equals("petFirstDate")||key.equals("petNeutralization")||key.equals("uid")) {continue;}
                    System.out.println("key of dateDB is " + key);
                    int year = Integer.parseInt(key.split("-")[0]);
                    int month = Integer.parseInt(key.split("-")[1]);
                    int day = Integer.parseInt(key.split("-")[2]);
                    dates.add(CalendarDay.from(year, month, day));
                    widget.removeDecorators();
                    widget.addDecorators(
                            new MySelectorDecorator(DiaryActivity.this, dates),
                            new HighlightWeekendsDecorator(),
                            oneDayDecorator,
                            todayDecorator
                    );
                    System.out.println("datesssssssssss size is "+dates.size());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        widget.setOnDateChangedListener(this);
        widget.setShowOtherDates(MaterialCalendarView.SHOW_ALL);

        final LocalDate instance = LocalDate.now();
        widget.setSelectedDate(instance);

        final LocalDate min = LocalDate.of(instance.getYear(), Month.JANUARY, 1);
        final LocalDate max = LocalDate.of(instance.getYear(), Month.DECEMBER, 31);

        widget.state().edit().setMinimumDate(min).setMaximumDate(max).commit();

        widget.invalidateDecorators();
        widget.addDecorators(
                new MySelectorDecorator(this, dates),
                new HighlightWeekendsDecorator(),
                oneDayDecorator,
                todayDecorator
        );

    }


    @Override
    public void onDateSelected(
            @NonNull MaterialCalendarView widget,
            @NonNull CalendarDay date,
            boolean selected) {
        //If you change a decorate, you need to invalidate decorators
        int Year = date.getYear();
        int Month = date.getMonth();
        int Day = date.getDay();

        String shot_Day = Year + "-" + Month + "-" + Day;

        Log.i("shot_Day test", shot_Day + "");
        widget.clearSelection();

        Toast.makeText(getApplicationContext(), shot_Day , Toast.LENGTH_SHORT).show();
        oneDayDecorator.setDate(date.getDate());
        widget.invalidateDecorators();

        if (!dates.contains(date)){
            Intent write_intent = new Intent(DiaryActivity.this, DiaryWriteActivity.class);
            write_intent.putExtra("petName", petName);
            write_intent.putExtra("saveDate", shot_Day);
            dates.add(date);
            startActivity(write_intent);
        }

        else {
            System.out.println("dates contain this date!!");
            diaryDB = FirebaseDatabase.getInstance().getReference("/pet/" + userID + "/" + petName);
            String sort_column_name = "title";
            Query sortbyTitle = diaryDB.orderByChild(sort_column_name);
            sortbyTitle.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    System.out.println("Done clear the diary data");
                    diary_data.clear();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        String key = postSnapshot.getKey();
                        if (key.equals(shot_Day)) {
                            DiaryClass get = postSnapshot.getValue(DiaryClass.class);
                            diary_data.add(get);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            Intent intent = new Intent(DiaryActivity.this, DiaryLargeActivity.class);
            intent.putExtra("petName", petName);
            startActivity(intent);
        }
    }

    @OnClick(R.id.button_weeks)
    public void onSetWeekMode() {
        widget.state().edit()
                .setCalendarDisplayMode(CalendarMode.WEEKS)
                .commit();
    }

    @OnClick(R.id.button_months)
    public void onSetMonthMode() {
        widget.state().edit()
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent;

        switch (id) {
            case R.id.nav_home:
                break;
            case R.id.nav_myCompanion:
                intent = new Intent(DiaryActivity.this, MyPetActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.nav_friends:
                intent = new Intent(DiaryActivity.this, SplashActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_board:
                intent = new Intent(DiaryActivity.this, BoardActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_share:
                intent = new Intent(DiaryActivity.this, NewsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_hospital_info:
                intent = new Intent(DiaryActivity.this, HospitalActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.nav_myInfo:
                intent = new Intent(DiaryActivity.this, MyInfoActivity.class);
                startActivity(intent);
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 1:
                break;
        }
    }


/*
    public void anim() {
        if (isFabOpen) {
            fabNewPost.startAnimation(fab_close);
            fabDeletePost.startAnimation(fab_close);
            fabNewPost.setClickable(false);
            fabDeletePost.setClickable(false);
            isFabOpen = false;
        } else {
            fabNewPost.startAnimation(fab_open);
            fabDeletePost.startAnimation(fab_open);
            fabNewPost.setClickable(true);
            fabDeletePost.setClickable(true);
            isFabOpen = true;
        }
    }
    */


}