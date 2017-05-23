package com.udacity.shahd.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.udacity.shahd.inventoryapp.data.InventoryContract.InventoryEntry;

import static com.udacity.shahd.inventoryapp.R.id.img;

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the product data loader
     */
    private static final int EXISTING_PRODUCT_LOADER = 0;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static int RESULT_LOAD_IMAGE = 1;
    /**
     * Content URI for the existing product (null if it's a new product)
     */
    private Uri mCurrentProductUri;
    /**
     * EditText fields
     */
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mSoldEditText;
    private EditText mStockEditText;
    private Button mStockButton;
    private Button mSaleButton;
    private Button mOrderButton;
    private Button mDeleteButton;
    private ImageView mImageView;
    private ImageButton mImageButton;
    private String picturePath;
    private boolean mProductHasChanged = false;
    private String imagePath;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        final Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_quantity);
        mSoldEditText = (EditText) findViewById(R.id.edit_sale);
        mStockEditText = (EditText) findViewById(R.id.edit_stock);
        mStockButton = (Button) findViewById(R.id.btn_stock);
        mSaleButton = (Button) findViewById(R.id.btn_sale);
        mOrderButton = (Button) findViewById(R.id.btn_order);
        mDeleteButton = (Button) findViewById(R.id.btn_delete);
        mImageView = (ImageView) findViewById(img);
        mImageButton = (ImageButton) findViewById(R.id.img_btn);

        // If the intent DOES NOT contain a product content URI, then we know that we are
        // creating a new product.
        if (mCurrentProductUri == null) {
            // This is a new product, so change the app bar to say "Add a product"
            setTitle(getString(R.string.editor_activity_title_new_product));
            mSoldEditText.setVisibility(View.INVISIBLE);
            mStockEditText.setVisibility(View.INVISIBLE);
            mSaleButton.setVisibility(View.INVISIBLE);
            mStockButton.setVisibility(View.INVISIBLE);
            mOrderButton.setVisibility(View.INVISIBLE);
            mDeleteButton.setVisibility(View.INVISIBLE);

        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit product"
            setTitle(getString(R.string.edit_product));

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);

            mSaleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String nameString = mNameEditText.getText().toString().trim();
                    String priceString = mPriceEditText.getText().toString().trim();
                    String soldString = mSoldEditText.getText().toString().trim();
                    String quantityString = mQuantityEditText.getText().toString().trim();
                    final ContentValues values = new ContentValues();
                    values.put(InventoryEntry.COLUMN_PRODUCT_NAME, nameString);
                    int price = 0;
                    int quantity = 0;
                    int sold = 0;
                    if (!TextUtils.isEmpty(soldString)) {
                        sold = Integer.parseInt(soldString);
                    }
                    if (!TextUtils.isEmpty(priceString)) {
                        price = Integer.parseInt(priceString);
                    }
                    if (!TextUtils.isEmpty(quantityString)) {
                        quantity = Integer.parseInt(quantityString);
                    }
                    if (picturePath == null) {
                        picturePath = imagePath;
                    }
//
                    Log.d("mImageButton", "setOnClickListener");
                    if (sold <= 0) {
                        Toast.makeText(getApplicationContext(), R.string.enter_valid_info,
                                Toast.LENGTH_LONG).show();
                    } else {
                        int newQuantity = quantity - sold;
                        if (newQuantity < 1) {
                            Toast.makeText(getApplicationContext(), R.string.try_another_sale_number,
                                    Toast.LENGTH_LONG).show();
                            newQuantity = quantity;
                        }
                        Log.d("mImageButton", "newQuantity: " + newQuantity + ", sold: " + sold);
                        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, price);
                        values.put(InventoryEntry.COLUMN_PRODUCT_PICTURE, picturePath);
                        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);
                        int newUri = getContentResolver().update(mCurrentProductUri, values, null, null);

                        // Show a toast message depending on whether or not the insertion was successful
                        if (newUri < 0) {
                            // If the new content URI is null, then there was an error with insertion.
                            Toast.makeText(getApplicationContext(), getString(R.string.editor_update_product_failed),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Otherwise, the insertion was successful and we can display a toast.
                            Toast.makeText(getApplicationContext(), getString(R.string.editor_update_droduct_successful),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });


        }

        mStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameString = mNameEditText.getText().toString().trim();
                String priceString = mPriceEditText.getText().toString().trim();
                String stockString = mStockEditText.getText().toString().trim();
                String quantityString = mQuantityEditText.getText().toString().trim();
                final ContentValues values = new ContentValues();
                values.put(InventoryEntry.COLUMN_PRODUCT_NAME, nameString);
                int price = 0;
                int quantity = 0;
                int stock = 0;
                if (!TextUtils.isEmpty(stockString)) {
                    stock = Integer.parseInt(stockString);
                }
                if (!TextUtils.isEmpty(priceString)) {
                    price = Integer.parseInt(priceString);
                }
                if (!TextUtils.isEmpty(quantityString)) {
                    quantity = Integer.parseInt(quantityString);
                }
                if (picturePath == null) {
                    picturePath = imagePath;
                }
//
                if (stock <= 0) {
                    Toast.makeText(getApplicationContext(), R.string.enter_valid_info,
                            Toast.LENGTH_LONG).show();
                } else {
                    quantity += stock;
                    Log.d("mImageButton", "price: " + price + ", stock: " + stock);
                    values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, price);
                    values.put(InventoryEntry.COLUMN_PRODUCT_PICTURE, picturePath);
                    values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);
                    int newUri = getContentResolver().update(mCurrentProductUri, values, null, null);

                    // Show a toast message depending on whether or not the insertion was successful
                    if (newUri < 0) {
                        // If the new content URI is null, then there was an error with insertion.
                        Toast.makeText(getApplicationContext(), getString(R.string.editor_update_product_failed),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Otherwise, the insertion was successful and we can display a toast.
                        Toast.makeText(getApplicationContext(), getString(R.string.editor_update_droduct_successful),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameString = mNameEditText.getText().toString().trim();
                String priceString = mPriceEditText.getText().toString().trim();
                String body = "Product name: " + nameString + "\n product price: " + priceString + "$ \n";
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"example@example.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Product Order: " + nameString);
                emailIntent.putExtra(Intent.EXTRA_TEXT, body);
                try {
                    startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                    finish();
                    Log.i("Finished sending email...", "");
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(DetailActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }

        });

        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("mImageButton", "setOnClickListener");
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });


        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSoldEditText.setOnTouchListener(mTouchListener);
        mStockEditText.setOnTouchListener(mTouchListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();
            Log.d("onActivityResult", picturePath);
            mImageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

        }


    }

    /**
     * Get user input from editor and save new product into database.
     */
    private void saveProduct() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String imgPath = picturePath;


        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(priceString) && TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(imgPath)) {
            return;
        }
        if (
                TextUtils.isEmpty(nameString) ||
                        TextUtils.isEmpty(priceString) || TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(imgPath)) {
            Toast.makeText(this, R.string.enter_valid_info, Toast.LENGTH_SHORT).show();
            return;
        }


        // If the weight is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int price = 0;
        int quantity = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }


        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(InventoryEntry.COLUMN_PRODUCT_PICTURE, imgPath);

        if (mCurrentProductUri == null) {
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();
            // Insert a new product into the provider, returning the content URI for the new product.
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
            } else {
            // Insert a new product into the provider, returning the content URI for the new product.
            int newUri = getContentResolver().update(mCurrentProductUri, values, null, null);

            // Show a toast message depending on whether or not the insertion was successful
            if (newUri < 0) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_droduct_successful),
                        Toast.LENGTH_SHORT).show();
            }
            }
        // Exit activity
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database
                saveProduct();

                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the product table
        String[] projection = {

                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_PICTURE
        };
        String idStr = mCurrentProductUri.toString().substring(mCurrentProductUri.toString().lastIndexOf('/') + 1);
        int id = Integer.parseInt(idStr);
        Log.d("id ", String.valueOf(id));

        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 0) {
            Log.d("cursor ", "cursor null");
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {

            // Find the columns of product attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            int imagePathColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PICTURE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            imagePath = cursor.getString(imagePathColumnIndex);
            Log.d("cursor name", name);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mPriceEditText.setText(Integer.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
            mImageView.setImageBitmap(BitmapFactory.decodeFile(imagePath));


        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void deleteProduct() {
        if (mCurrentProductUri != null) {
            String idStr = mCurrentProductUri.toString().substring(mCurrentProductUri.toString().lastIndexOf('/') + 1);
            int id = Integer.parseInt(idStr);
            Log.d("mCurrentProductUri ", mCurrentProductUri.toString());
            Log.d("id ", String.valueOf(id));
            // This loader will execute the ContentProvider's query method on a background thread

            // Insert a new product into the provider, returning the content URI for the new product.
            int newUri = getContentResolver().delete(InventoryEntry.CONTENT_URI, InventoryEntry._ID + "=?", new String[]{idStr});

            // Show a toast message depending on whether or not the insertion was successful
            if (newUri < 0) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(getBaseContext(), getString(R.string.editor_delete_product_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(getBaseContext(), getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
                // Close the activity
                finish();
            }
        }
    }
}

