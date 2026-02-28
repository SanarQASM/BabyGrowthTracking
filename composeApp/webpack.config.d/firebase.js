// Fix Node.js polyfill warnings
config.resolve = config.resolve || {};
config.resolve.fallback = {
    ...(config.resolve.fallback || {}),
    "os": false,
    "path": false,
    "fs": false,
    "crypto": false
};

// Inject Firebase scripts before composeApp.js
const HtmlWebpackPlugin = require('html-webpack-plugin');

config.plugins = config.plugins.filter(
    plugin => !(plugin instanceof HtmlWebpackPlugin)
);

config.plugins.push(
    new HtmlWebpackPlugin({
        templateContent: `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Baby Growth Track</title>
    <link rel="stylesheet" href="styles.css">
    <script src="https://www.gstatic.com/firebasejs/10.7.0/firebase-app-compat.js"></script>
    <script src="https://www.gstatic.com/firebasejs/10.7.0/firebase-auth-compat.js"></script>
    <script src="https://www.gstatic.com/firebasejs/10.7.0/firebase-analytics-compat.js"></script>
    <script>
        try {
            const firebaseConfig = {
                apiKey: "AIzaSyAIUFHXzCKuOVN23DjYALmNT17Yyt5s4a4",
                authDomain: "babygrowthtracking-cfd66.firebaseapp.com",
                projectId: "babygrowthtracking-cfd66",
                storageBucket: "babygrowthtracking-cfd66.firebasestorage.app",
                messagingSenderId: "887110744705",
                appId: "1:887110744705:web:6372e4d47c402ac5062b80",
                measurementId: "G-LVCEYYKNYB"
            };
            firebase.initializeApp(firebaseConfig);
            firebase.analytics();
            window.__firebaseReady = true;
            console.log("Firebase initialized successfully");
        } catch(e) {
            console.error("Firebase initialization failed:", e);
            window.__firebaseReady = false;
        }
    </script>
    <script src="https://accounts.google.com/gsi/client" async defer></script>
</head>
<body>
<div id="ComposeTarget"></div>
</body>
</html>`,
        inject: 'body',
        scriptLoading: 'blocking'
    })
);