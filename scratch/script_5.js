
    window.firebaseConfig = {
      apiKey: "AIzaSyCiKGZ_J8p-XnvsANNnmkrUxEZQX0KUVUc",
      authDomain: "v07-dashboard.firebaseapp.com",
      projectId: "v07-dashboard",
      storageBucket: "v07-dashboard.firebasestorage.app",
      messagingSenderId: "689369760342",
      appId: "1:689369760342:web:e0debabebc08d6f016f1f7"
    };
    var firebaseConfig = window.firebaseConfig;

    window.ensureFirebaseInitialized = function() {
      if (typeof firebase === 'undefined') return false;
      try {
        if (!firebase.apps || firebase.apps.length === 0) {
          firebase.initializeApp(window.firebaseConfig);
        }
        return (firebase.apps && firebase.apps.length > 0);
      } catch (e) {
        console.warn('[V07 Firebase] ensureFirebaseInitialized:', e);
        return (firebase.apps && firebase.apps.length > 0);
      }
    };

    try {
      window.ensureFirebaseInitialized();
      if (typeof firebase !== 'undefined' && firebase.auth) {
        try {
          firebase.auth().setPersistence((firebase.auth.Auth && firebase.auth.Auth.Persistence && firebase.auth.Auth.Persistence.LOCAL) || 'local').catch(function(){});
        } catch (_) {}
      }
    } catch (e) {
      console.warn('[V07 Firebase Init]:', e);
    }
  