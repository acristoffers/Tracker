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

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.EditText;

public class PackageSearchEditText extends EditText {

    @SuppressWarnings("unused")
    public PackageSearchEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        addTextChangedListener(new TrackCodeFormattingTextWatcher());
    }

    @SuppressWarnings("unused")
    public PackageSearchEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        addTextChangedListener(new TrackCodeFormattingTextWatcher());
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        int cursor = getSelectionStart();
        int type = getInputType();
        if (cursor > 1 && cursor < 11) {
            if (type != InputType.TYPE_CLASS_NUMBER) {
                setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        } else {
            if (type != InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS) {
                setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            }
        }
    }

}
