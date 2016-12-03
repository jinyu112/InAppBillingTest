package com.tidisventures.inappbilling;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.tidisventures.inappbilling.util.IabHelper;
import com.tidisventures.inappbilling.util.IabResult;
import com.tidisventures.inappbilling.util.Inventory;
import com.tidisventures.inappbilling.util.Purchase;

public class MainActivity extends Activity {
    private Button clickButton;
    private Button buyButton;
    private static final String TAG = "poop";
    static final String ITEM_SKU = "android.test.purchased";
    IabHelper mHelper;
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result,
                                          Purchase purchase)
        {
            if (result.isFailure()) {
                // Handle error
                return;
            }
            else if (purchase.getSku().equals(ITEM_SKU)) {
                consumeItem();
                buyButton.setEnabled(false);
            }

        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase,
                                              IabResult result) {

                    if (result.isSuccess()) {
                        clickButton.setEnabled(true);
                    } else {
                        // handle error
                    }
                }
            };

    @Override
    protected void onStart() {
        super.onStart();
        buyButton = (Button)findViewById(R.id.buyButton);
        clickButton = (Button)findViewById(R.id.clickButton);
        clickButton.setEnabled(false);

        String base64EncodedPublicKey =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiazdqMpRRC9nVlZ9BEwDlsi2R19qXFHVM+oSUV+WJIl/C26DseKqBl9t+ZTIYXHRwEMjfjBnoZqF/KK+zzNnclDiFHNREaBXzYs4nWAjVQwRUOiS2GXzXt3BGuKvBTctYVrguX1dlf5FjW6SN0QIFhHMrluUSohHXPTeBGlKdvorHNr7lrU1nHrUuvLqwK/K8AtXTmHApsaa7+0x5bS/6xPlMJEtKLJ/iJ6V0SNLZ4OE8LXbb7OQE/bqenlmGKaoA4/1Qk7utF2uCXnqk4JQ4upBELW81JTmg6MxeY5a06qid3T9Gn1IoZdT3ifEVp/pGm7Wsq56J0RHOZZljcY7cQIDAQAB";

        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
           public void onIabSetupFinished(IabResult result)
           {
               if (!result.isSuccess()) {
                   Log.d(TAG, "In-app Billing setup failed:" + result);
               } else {
                   Log.d(TAG, "In-app Billing is set up OK");
               }
           }
       });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void buyClick(View view)
    {
        try {
            mHelper.launchPurchaseFlow(this, ITEM_SKU, 10001,
                    mPurchaseFinishedListener, "mypurchasetoken");
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data)
    {
        if (!mHelper.handleActivityResult(requestCode,
                resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void consumeItem() {
        try {
            mHelper.queryInventoryAsync(mReceivedInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {

            if (result.isFailure()) {
                // Handle failure
            } else {
                try {
                    mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU),
                            mConsumeFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }
            }
        }
    };



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) try {
            mHelper.dispose();
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
        mHelper = null;
    }
}
