/*
 * Copyright (c) 2011 Brian J. Tarricone <brian@tarricone.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.spurint.android.listview;

import android.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SectionHeaderRow extends ListRow {
    private final String title;

    public SectionHeaderRow(String title) {
        this.title = title;
    }

    @Override
    public View getView(Context context, View convertView)
    {
    	final LinearLayout layout = new LinearLayout(context);

    	final LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final TextView text = (TextView)inflater.inflate(R.layout.preference_category, null);
        android.util.Log.d("foo", "inflated pref category is a " + text.getClass().getName());
        text.setText(title);
        
        layout.addView(text,
        			   LinearLayout.LayoutParams.MATCH_PARENT,
        			   LinearLayout.LayoutParams.MATCH_PARENT);

        layout.setOnClickListener(null);
        layout.setOnLongClickListener(null);
        layout.setClickable(false);
        layout.setLongClickable(false);

        return layout;
    }

    @Override
    public String getTitle() {
        return title;
    }
    
    @Override
    public boolean isEnabled()
    {
        return false;
    }
}
