const https = require('https');

https.get('https://v07-dashboard.web.app', (res) => {
  let data = '';
  res.on('data', chunk => data += chunk);
  res.on('end', () => {
    console.log('HTTP Status:', res.statusCode);
    console.log('Total Bytes:', data.length);
    console.log('Has firebaseConfig:', data.includes('AIzaSyCiKGZ_J8p-XnvsANNnmkrUxEZQX0KUVUc'));
    console.log('Has authLoggedInView:', data.includes('authLoggedInView'));
    console.log('Has authLoggedOutView:', data.includes('authLoggedOutView'));
    console.log('Has doc(uid):', data.includes('.doc(uid)'));
    console.log('Any .document(:', data.includes('.document('));
  });
}).on('error', err => {
  console.error('Error fetching live website:', err);
});
