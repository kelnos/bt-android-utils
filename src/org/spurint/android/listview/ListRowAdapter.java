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

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class ListRowAdapter extends ArrayAdapter<ListRow> {
    private final List<ListRow> items;
    private final Context context;
    private final int nViewTypes;

    public ListRowAdapter(Context context, List<ListRow> items, int nViewTypes)
    {
        super(context, 0, items);
        this.items = items;
        this.context = context;
        this.nViewTypes = nViewTypes;
    }
    
    public ListRowAdapter(Context context, List<ListRow> items)
    {
        this(context, items, 1);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = null;

        ListRow row = items.get(position);
        if (row != null)
            v = row.getView(context, convertView);

        return v;
    }

    @Override
    public boolean isEnabled(int position)
    {
        return items.get(position).isEnabled();
    }
    
    @Override
    public int getViewTypeCount()
    {
        return nViewTypes;
    }
    
    @Override
    public int getItemViewType(int position)
    {
        return items.get(position).getItemViewType();
    }
}
