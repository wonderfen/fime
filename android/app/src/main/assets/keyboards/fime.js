!(function (w, d) {
    const fime = {}

    let callbacks = {}
    let listeners = {}
    function nextId() {
        return (new Date).getTime()
    }

    let hasBridge_ = false
    function hasBridge() {
        if (hasBridge_ === true) return true
        return hasBridge_ = w.android && w.android.jsCallNative
    }

    fime.callNative = function (action, args, callback) {
        if (hasBridge()) {
            // console.log('jsCallNative')
            const id = nextId()
            w.android.jsCallNative(id, action, JSON.stringify(args))
            if (callback) {
                callbacks[id] = callback
                setTimeout(() => {
                    delete callbacks[id]
                }, 3000)
            }
        } else {
            console.warn('Only works on Android！')
        }
    }

    fime.nativeCallback = function (id, args) {
        if (callbacks[id]) {
            callbacks[id](args)
            delete callbacks[id]
        }
    }

    fime.onNativeCall = function (name, args) {
        if (listeners[name]) listeners[name](args)
    }

    fime.addListener = function (name, fn) {
        listeners[name] = fn
    }

    fime.onKey = function (name) {
        fime.callNative('onKey', { name: name })
    }

    fime.setMode = function (mode) {
        fime.callNative('setMode', { mode: mode })
    }

    fime.qs = function (selector) {
        return d.querySelector(selector)
    }

    fime.qsa = function (selector) {
        return d.querySelectorAll(selector)
    }

    w.fime = fime
})(window, document);
