package com.madcamp.petclub.Diary.decorators;

import android.app.Activity;
import android.graphics.drawable.Drawable;

import com.madcamp.petclub.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.ArrayList;
import java.util.HashSet;

public class MySelectorDecorator implements DayViewDecorator {

    private final Drawable drawable;
    private CalendarDay date;
    private HashSet<CalendarDay> dates;

    public MySelectorDecorator(Activity context, ArrayList<CalendarDay> dates) {
        drawable = context.getResources().getDrawable(R.drawable.dog);
        date = CalendarDay.today();
        this.dates = new HashSet<>(dates);
    }


    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setSelectionDrawable(drawable);
    }

}
