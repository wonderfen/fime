let callbacks = {}

function nextId() {
    return (new Date).getTime()
}

function isJsBridgeEnabled() {
    return window.android && window.android.jsCallNative
}

function jsCallNative(action, args) {
    if (isJsBridgeEnabled()) {
        console.log('jsCallNative')
        window.android.jsCallNative(nextId(), action, JSON.stringify(args))
    } else {
        console.warn('在非 android 环境中调用无效！')
    }
}

//export {
//    isEnabled as isJsBridgeEnabled,
//    jsCallNative as callNative
//}