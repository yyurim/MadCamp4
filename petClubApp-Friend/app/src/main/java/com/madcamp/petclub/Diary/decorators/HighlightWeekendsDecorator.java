package com.madcamp.petclub.Diary.decorators;

import android.graphics.Color;
import android.text.style.ForegroundColorSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import org.threeten.bp.DayOfWeek;

public class HighlightWeekendsDecorator implements DayViewDecorator {

//    private final Drawable highlightDrawable;
//    private final Drawable drawable;
//    private static final int color = Color.parseColor("#FFF");

    public HighlightWeekendsDecorator() {
    }

    @Override
    public boolean shouldDecorate(final CalendarDay day) {
        final DayOfWeek weekDay = day.getDate().getDayOfWeek();
        return weekDay == DayOfWeek.SATURDAY || weekDay == DayOfWeek.SUNDAY;
    }

    @Override
    public void decorate(final DayViewFacade view) {
        view.addSpan(new ForegroundColorSpan(Color.RED));
    }
}

