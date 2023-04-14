!(function (w, d) {
    const fime = {}

    let callbacks = {}

    function nextId() {
        return (new Date).getTime()
    }

    function hasBridge() {
        return w.android && w.android.jsCallNative
    }

    fime.callNative = function (action, args) {
        if (hasBridge()) {
            console.log('jsCallNative')
            w.android.jsCallNative(nextId(), action, JSON.stringify(args))
        } else {
            console.warn('Only works on Android！')
        }
    }

    fime.qs = function (selector) {
        return d.querySelector(selector)
    }

    fime.qsa = function (selector) {
        return d.querySelectorAll(selector)
    }

    w.fime = fime
})(window, document);
