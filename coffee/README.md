# Coffee Shop App

A native Android coffee shop exercise with a custom RecyclerView product adapter, espresso/brewed/cold/tea categories, search, item details, size selection, quantity-aware cart, pickup name, and order confirmation.

Open the project root in Android Studio with JDK 17/21 and Android SDK 35. Keep the `common` module beside `coffee`, then run `:coffee:assembleDebug :coffee:testDebugUnitTest :coffee:lintDebug`.

The cart is private to the device and survives app restart. Checkout is a local training flow and does not charge a payment method or send an order to a server.
