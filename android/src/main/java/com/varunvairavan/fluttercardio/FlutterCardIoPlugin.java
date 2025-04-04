package com.varunvairavan.fluttercardio;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import io.card.payment.CardIOActivity;
import io.card.payment.CardType;
import io.card.payment.CreditCard;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener;
import io.flutter.embedding.engine.plugins.FlutterPlugin;

/**
 * FlutterCardIoPlugin
 */
public class FlutterCardIoPlugin implements MethodCallHandler, FlutterPlugin, ActivityAware, PluginRegistry.ActivityResultListener {
    private static final int MY_SCAN_REQUEST_CODE = 100;

    private Result pendingResult;
    private MethodCall methodCall;
    private MethodChannel channel;
    private ActivityPluginBinding activityPluginBinding;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_card_io");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    @Override
    public void onDetachedFromActivity() {}
    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding activityPluginBinding) {
        onAttachedToActivity(activityPluginBinding);
    }
    @Override
    public void onAttachedToActivity(ActivityPluginBinding activityPluginBinding) {
        activityPluginBinding.addActivityResultListener(this);
        this.activityPluginBinding = activityPluginBinding;
    }
    @Override
    public void onDetachedFromActivityForConfigChanges() {}

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (pendingResult != null) {
            result.error("ALREADY_ACTIVE", "Scan card is already active", null);
            return;
        }

        pendingResult = result;
        methodCall = call;

        if (call.method.equals("scanCard")) {
            Intent scanIntent = new Intent(this.activityPluginBinding.getActivity(), CardIOActivity.class);

            boolean requireExpiry = false;
            if (methodCall.hasArgument("requireExpiry")) {
                requireExpiry = methodCall.argument("requireExpiry");
            }

            boolean requireCVV = false;
            if (methodCall.hasArgument("requireCVV")) {
                requireCVV = methodCall.argument("requireCVV");
            }

            boolean requirePostalCode = false;
            if (methodCall.hasArgument("requirePostalCode")) {
                requirePostalCode = methodCall.argument("requirePostalCode");
            }

            boolean requireCardHolderName = false;
            if (methodCall.hasArgument("requireCardHolderName")) {
                requireCardHolderName = methodCall.argument("requireCardHolderName");
            }

            boolean restrictPostalCodeToNumericOnly = false;
            if (methodCall.hasArgument("restrictPostalCodeToNumericOnly")) {
                restrictPostalCodeToNumericOnly = methodCall.argument("restrictPostalCodeToNumericOnly");
            }

            boolean scanExpiry = true;
            if (methodCall.hasArgument("scanExpiry")) {
                scanExpiry = methodCall.argument("scanExpiry");
            }

            String scanInstructions = null;
            if (methodCall.hasArgument("scanInstructions")) {
                scanInstructions = methodCall.argument("scanInstructions");
            }

            boolean suppressManualEntry = false;
            if (methodCall.hasArgument("suppressManualEntry")) {
                suppressManualEntry = methodCall.argument("suppressManualEntry");
            }

            boolean suppressConfirmation = false;
            if (methodCall.hasArgument("suppressConfirmation")) {
                suppressConfirmation = methodCall.argument("suppressConfirmation");
            }

            boolean useCardIOLogo = false;
            if (methodCall.hasArgument("useCardIOLogo")) {
                useCardIOLogo = methodCall.argument("useCardIOLogo");
            }

            boolean hideCardIOLogo = false;
            if (methodCall.hasArgument("hideCardIOLogo")) {
                hideCardIOLogo = methodCall.argument("hideCardIOLogo");
            }

            boolean usePayPalActionbarIcon = true;
            if (methodCall.hasArgument("usePayPalActionbarIcon")) {
                usePayPalActionbarIcon = methodCall.argument("usePayPalActionbarIcon");
            }

            boolean keepApplicationTheme = false;
            if (methodCall.hasArgument("keepApplicationTheme")) {
                keepApplicationTheme = methodCall.argument("keepApplicationTheme");
            }

            // customize these values to suit your needs.
            scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, requireExpiry); // default: false
            scanIntent.putExtra(CardIOActivity.EXTRA_SCAN_EXPIRY, scanExpiry);
            scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, requireCVV); // default: false
            scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, requirePostalCode); // default: false
            scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CARDHOLDER_NAME, requireCardHolderName);
            scanIntent.putExtra(CardIOActivity.EXTRA_RESTRICT_POSTAL_CODE_TO_NUMERIC_ONLY, restrictPostalCodeToNumericOnly);
            scanIntent.putExtra(CardIOActivity.EXTRA_SCAN_INSTRUCTIONS, scanInstructions);
            scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, suppressManualEntry);
            scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_CONFIRMATION, suppressConfirmation);
            scanIntent.putExtra(CardIOActivity.EXTRA_USE_CARDIO_LOGO, useCardIOLogo);
            scanIntent.putExtra(CardIOActivity.EXTRA_HIDE_CARDIO_LOGO, hideCardIOLogo);
            scanIntent.putExtra(CardIOActivity.EXTRA_USE_PAYPAL_ACTIONBAR_ICON, usePayPalActionbarIcon);
            scanIntent.putExtra(CardIOActivity.EXTRA_KEEP_APPLICATION_THEME, keepApplicationTheme);

            // MY_SCAN_REQUEST_CODE is arbitrary and is only used within this activity.
            this.activityPluginBinding.getActivity().startActivityForResult(scanIntent, MY_SCAN_REQUEST_CODE);
        } else {
            result.notImplemented();
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_SCAN_REQUEST_CODE) {
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

                Map<String, Object> response = new HashMap<>();
                response.put("cardholderName", scanResult.cardholderName);
                response.put("cardNumber", scanResult.cardNumber);
                String cardType = null;
                if (scanResult.getCardType() != CardType.UNKNOWN && scanResult.getCardType() != CardType.INSUFFICIENT_DIGITS) {
                    switch (scanResult.getCardType()) {
                        case AMEX:
                            cardType = "Amex";
                            break;
                        case DINERSCLUB:
                            cardType = "DinersClub";
                            break;
                        case DISCOVER:
                            cardType = "Discover";
                            break;
                        case JCB:
                            cardType = "JCB";
                            break;
                        case MASTERCARD:
                            cardType = "MasterCard";
                            break;
                        case VISA:
                            cardType = "Visa";
                            break;
                        case MAESTRO:
                            cardType = "Maestro";
                            break;
                        default:
                            break;
                    }
                }
                response.put("cardType", cardType);
                response.put("redactedCardNumber", scanResult.getRedactedCardNumber());
                response.put("expiryMonth", scanResult.expiryMonth);
                response.put("expiryYear", scanResult.expiryYear);
                response.put("cvv", scanResult.cvv);
                response.put("postalCode", scanResult.postalCode);
                pendingResult.success(response);
            } else {
                pendingResult.success(null);
            }
            pendingResult = null;
            methodCall = null;
            return true;
        }
        return false;
    }
}
