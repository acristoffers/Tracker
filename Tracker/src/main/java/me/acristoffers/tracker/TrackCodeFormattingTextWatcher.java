/*
 * Copyright (c) 2014 Álan Crístoffer
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package me.acristoffers.tracker;

import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.BulletSpan;
import android.text.style.ClickableSpan;
import android.text.style.DrawableMarginSpan;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.IconMarginSpan;
import android.text.style.ImageSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.MaskFilterSpan;
import android.text.style.MetricAffectingSpan;
import android.text.style.QuoteSpan;
import android.text.style.RasterizerSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.ReplacementSpan;
import android.text.style.ScaleXSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.TabStopSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrackCodeFormattingTextWatcher implements TextWatcher {

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        String regex = "[A-Z]{2}[0-9]{9}[A-Z]{2}";
        String text = editable.toString();

        resetTextSpans(editable);

        if (text.matches(".*[a-z].*")) {
            text = text.toUpperCase(Locale.US);
            editable.replace(0, editable.length(), text);
        }

        if (text.matches(regex)) {
            return;
        }

        text = text.toUpperCase(Locale.US);
        if (text.matches(regex)) {
            editable.replace(0, editable.length(), text);
        } else {
            Pattern pattern = Pattern.compile("([A-Za-z]{2}[0-9]{9}[A-Za-z]{2})");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                text = matcher.group(1);
                editable.replace(0, editable.length(), text);
            }
        }
    }

    private void resetTextSpans(Editable editable) {
        removeSpan(editable, AbsoluteSizeSpan.class);
        removeSpan(editable, AlignmentSpan.class);
        removeSpan(editable, BackgroundColorSpan.class);
        removeSpan(editable, BulletSpan.class);
        removeSpan(editable, ClickableSpan.class);
        removeSpan(editable, DrawableMarginSpan.class);
        removeSpan(editable, DynamicDrawableSpan.class);
        removeSpan(editable, ForegroundColorSpan.class);
        removeSpan(editable, IconMarginSpan.class);
        removeSpan(editable, ImageSpan.class);
        removeSpan(editable, LeadingMarginSpan.class);
        removeSpan(editable, MaskFilterSpan.class);
        removeSpan(editable, MetricAffectingSpan.class);
        removeSpan(editable, QuoteSpan.class);
        removeSpan(editable, RasterizerSpan.class);
        removeSpan(editable, RelativeSizeSpan.class);
        removeSpan(editable, ReplacementSpan.class);
        removeSpan(editable, ScaleXSpan.class);
        removeSpan(editable, StrikethroughSpan.class);
        removeSpan(editable, StyleSpan.class);
        removeSpan(editable, SubscriptSpan.class);
        removeSpan(editable, SuperscriptSpan.class);
        removeSpan(editable, TabStopSpan.class);
        removeSpan(editable, TextAppearanceSpan.class);
        removeSpan(editable, TypefaceSpan.class);
        removeSpan(editable, UnderlineSpan.class);
        removeSpan(editable, URLSpan.class);

        editable.setSpan(new ForegroundColorSpan(0xFFFFFFFF), 0, editable.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        editable.setSpan(new BackgroundColorSpan(0x00000000), 0, editable.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }

    @SuppressWarnings("unchecked")
    private void removeSpan(Editable editable, Class spanClass) {
        Object spans[] = editable.getSpans(0, editable.length(), spanClass);
        for (Object span : spans) {
            editable.removeSpan(span);
        }
    }

}
