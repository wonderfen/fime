!(function (w, d) {
    const fime = {}

    let callbacks = {}
    function nextId() {
        return (new Date).getTime()
    }

    let hasBridge_ = false
    function hasBridge() {
        if (hasBridge === true) return true
        hasBridge_ = w.android && w.android.jsCallNative
        return hasBridge_
    }

    fime.callNative = function (action, args) {
        if (hasBridge()) {
            // console.log('jsCallNative')
            w.android.jsCallNative(nextId(), action, JSON.stringify(args))
        } else {
            console.warn('Only works on Android！')
        }
    }

    fime.onKey = function (name) {
        fime.callNative('onKey', name)
    }

    fime.setMode = function (mode) {
        fime.callNative('setMode', mode)
    }

    fime.qs = function (selector) {
        return d.querySelector(selector)
    }

    fime.qsa = function (selector) {
        return d.querySelectorAll(selector)
    }

    w.fime = fime
})(window, document);
