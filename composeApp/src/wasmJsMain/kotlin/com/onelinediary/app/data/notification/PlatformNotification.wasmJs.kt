package com.onelinediary.app.data.notification

@JsFun("(title, body) => { try { if (typeof Notification !== 'undefined') { if (Notification.permission === 'granted') { new Notification(title, { body: body }); } else if (Notification.permission !== 'denied') { Notification.requestPermission().then(function(perm) { if (perm === 'granted') new Notification(title, { body: body }); }); } } } catch(e) {} }")
private external fun jsTriggerNotification(title: String, body: String)

actual object PlatformNotification {
    actual fun showNotification(title: String, message: String) {
        jsTriggerNotification(title, message)
    }
}
