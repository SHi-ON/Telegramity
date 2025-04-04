/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gramity.calendar.persianmaterialdatepicker.date;

import android.content.Context;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ViewAnimator;

import org.gramity.GramityUtilities;
import org.gramity.calendar.persianmaterialdatepicker.utils.PersianCalendar;

public class AccessibleDateAnimator extends ViewAnimator {
    private long mDateMillis;

    public AccessibleDateAnimator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDateMillis(long dateMillis) {
        mDateMillis = dateMillis;
    }

    /**
     * Announce the currently-selected date when launched.
     */
    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // Clear the event's current text so that only the current date will be spoken.
            event.getText().clear();
            PersianCalendar mPersianCalendar = new PersianCalendar();
            mPersianCalendar.setTimeInMillis(mDateMillis);
            String dateString = GramityUtilities.getPersianNumbering(
                    mPersianCalendar.getPersianMonthName() + " " +
                    mPersianCalendar.getPersianYear()
            );
            event.getText().add(dateString);
            return true;
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }
}