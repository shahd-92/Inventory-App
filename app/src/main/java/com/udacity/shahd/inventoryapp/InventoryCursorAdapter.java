package com.udacity.shahd.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.shahd.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by shahd on 5/20/17.
 */

class InventoryCursorAdapter extends CursorAdapter {
    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);

    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // Find fields to populate in inflated template
        TextView tvName = (TextView) view.findViewById(R.id.name);
        TextView tvQuantity = (TextView) view.findViewById(R.id.quantity);
        TextView tvPrice = (TextView) view.findViewById(R.id.price);
        ImageView imageView = (ImageView) view.findViewById(R.id.img);
        Button btnSale = (Button) view.findViewById(R.id.sale);
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.item_container);
        // Extract properties from cursor
        final String id = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry._ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_NAME));
        int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_QUANTITY));
        int price = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_PRICE));
        String imgPath = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PICTURE));
        // Populate fields with extracted properties
        tvName.setText(name);
        tvQuantity.setText(String.valueOf(quantity));
        tvPrice.setText(String.valueOf(price));

        int newQuantity = quantity - 1;
        if (newQuantity < 1) {
            Toast.makeText(context, R.string.try_another_sale_number,
                    Toast.LENGTH_LONG).show();
            newQuantity = price;
        }
        final Uri uriId = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, Long.parseLong(id));
        final ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, name);
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, (price));
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);
        values.put(InventoryEntry.COLUMN_PRODUCT_PICTURE, imgPath);

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("setOnItemClickListener", InventoryEntry.CONTENT_URI.toString());
                Intent intent = new Intent(context, DetailActivity.class);
                Uri uriId = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, Long.parseLong(id));
                intent.setData(uriId);
                context.startActivity(intent);
            }
        });

        btnSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newUri = context.getContentResolver().update(uriId, values, null, null);

                // Show a toast message depending on whether or not the insertion was successful
                if (newUri < 0) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(context, context.getString(R.string.editor_update_product_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(context, context.getString(R.string.editor_update_droduct_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


}
