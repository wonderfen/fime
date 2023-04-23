!(function (w, d) {
    const CN_MODE = 1
    const EN_MODE = 0

    const ESC = 'VK_FN_ESC'
    const TAB = 'VK_FN_TAB'
    const SHIFT = 'VK_FN_SHIFT'
    const CTRL = 'VK_FN_CTRL'
    const ALT = 'VK_FN_ALT'
    const BACKSPACE = 'VK_FN_BACKSPACE'
    const DELETE = 'VK_FN_DELETE'
    const HOME = 'VK_FN_HOME'
    const END = 'VK_FN_END'
    const PAGE_UP = 'VK_FN_PAGE_UP'
    const PAGE_DOWN = 'VK_FN_PAGE_DOWN'
    const ENTER = 'VK_FN_ENTER'
    const CLEAR = 'VK_FN_CLEAR'
    const CAPS_LOCK = 'VK_FN_CAPS_LOCK'
    const LEFT = 'VK_FN_LEFT'
    const UP = 'VK_FN_UP'
    const RIGHT = 'VK_FN_RIGHT'
    const DOWN = 'VK_FN_DOWN'
    const ABC = 'VK_FN_ABC'
    const DIGITAL = 'VK_FN_123'
    const SYMBOL = 'VK_FN_SYMBOL'
    const RETURN = 'VK_FN_RETURN'
    const SPACE = 'VK_SPACE'
    const SINGLE_QUOTE = 'VK_SINGLE_QUOTE'

    const REG_ALPHABET = new RegExp("^[a-zA-Z]$")

    const fime = {}

    let callbacks = {}
    let listeners = {}

    function getTimestamp() {
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
            const id = getTimestamp()
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

    function getKeyName(el) {
        return el.dataset['name'] || el.querySelector('.fime-label').innerHTML
    }

    const tapTimeout = 200
    const longPressInterval = 150
    let timer = null
    let touch = {
        keyboard: null
    }
    const tan30d = Math.tan(Math.PI / 6)

    function startLongPress() {
        if (timer) clearInterval(timer)
        timer = setInterval(function () {
            if (touch.longPressStarted) {
                touch.longPressKeep = true
                // console.log('long press @' + touch.at)
                if (touch.at && touch.keyboard) touch.keyboard.onLongPress(touch.at)
            } else {
                clearInterval(timer)
                timer = null
            }
        }, longPressInterval)
    }

    function onTouchStart(e) {
        e.preventDefault()
        if (e.target.tagName == 'SPAN') {
            touch.at = e.target.parentNode
        } else if (e.target.classList.contains('fime-key')) {
            touch.at = e.target
        } else {
            console.log('touch at blank area.')
            delete touch.at
            return
        }
        touch.at.classList.add('fime-key-active')
        touch.startTime = getTimestamp()
        touch.start = e.touches[0]
        // console.log('touchstart: touch=' + JSON.stringify(touch))
        touch.longPressStarted = true
        startLongPress()
    }

    function onTouchMove(e) {
        touch.longPressStarted = false
        if (!touch.longPressKeep) {
            touch.moveTo = e.touches[0]
            // console.log('touchmove: target=' + JSON.stringify(touch))
        }
    }

    function onTouchEnd(e) {
        const at = touch.at
        const start = touch.start
        const moveTo = touch.moveTo
        touch.longPressStarted = false
        touch.longPressKeep = false
        delete touch.at
        delete touch.moveTo
        if (!at) {
            console.log('ignored touch event.')
            return
        }
        // console.log('at.classList=' + at.classList)
        setTimeout(() => at.classList.remove('fime-key-active'), 50)
        if (timer) clearInterval(timer)
        if (!touch.keyboard) return

        const kbd = touch.keyboard
        if (start && moveTo) { // swipe
            let dx = moveTo.screenX - start.screenX
            let dy = moveTo.screenY - start.screenY
            if (dx * dx + dy * dy <= 8) {
                console.log('assume tap end')
                kbd.onTap(at)
            } else {
                if (Math.abs(dy) / Math.abs(dx) <= tan30d) {   // swipe in x direction
                    if (dx > 0) {
                        kbd.onSwipe(at, 'right')
                    } else {
                        kbd.onSwipe(at, 'left')
                    }
                } else {    // swipe in y direction
                    if (dy > 0) {
                        kbd.onSwipe(at, 'down')
                    } else {
                        kbd.onSwipe(at, 'up')
                    }
                }
            }
        } else if (getTimestamp() - touch.startTime <= tapTimeout) {    // tap
            kbd.onTap(at)
        } else {    // long press end
            console.log('long press end')
        }
    }

    function onTouchCancel(e) {
        console.log('touchcancel')
        if (timer) {
            clearInterval(timer)
            timer = null
        }
        delete touch.at
        delete touch.moveTo
        touch.longPressStarted = false
        touch.longPressKeep = false
    }


    let keyboards = {}
    let handler = null

    class Keyboard {
        constructor(layout, el) {
            this.layout = layout
            this.el = el
            this.shifted = false
            this.swipes = {}
            this.swipesEnable = true
            this.init()
        }

        init() {
            this.disableTouch()
            this.enableTouch()
            this.updateKeyName()
        }

        disableTouch() {
            this.el.removeEventListener('touchstart', onTouchStart)
            this.el.removeEventListener('touchmove', onTouchMove)
            this.el.removeEventListener('touchend', onTouchEnd)
            this.el.removeEventListener('touchcancel', onTouchCancel)
        }

        enableTouch() {
            touch.keyboard = this
            this.el.addEventListener('touchstart', onTouchStart, { passive: false })
            this.el.addEventListener('touchmove', onTouchMove, { passive: false })
            this.el.addEventListener('touchend', onTouchEnd, { passive: false })
            this.el.addEventListener('touchcancel', onTouchCancel, { passive: false })
        }

        disableSwipe() {
            this.swipesEnable = false
        }

        enableSwipe(options) {
            this.swipesEnable = true
            for (let key in options) {
                this.swipes[key] = options[key]
            }
        }

        updateKeyName() {
            this.el.querySelectorAll('.fime-key').forEach(k => {
                let name = getKeyName(k)
                if (!name) {
                    if (k.classList.contains('key-esc')) {
                        k.dataset.name = ESC
                    } else if (k.classList.contains('key-tab')) {
                        k.dataset.name = TAB
                    } else if (k.classList.contains('key-shift')) {
                        k.dataset.name = SHIFT
                    } else if (k.classList.contains('key-ctrl')) {
                        k.dataset.name = CTRL
                    } else if (k.classList.contains('key-alt')) {
                        k.dataset.name = ALT
                    } else if (k.classList.contains('key-backspace')) {
                        k.dataset.name = BACKSPACE
                    } else if (k.classList.contains('key-delete')) {
                        k.dataset.name = DELETE
                    } else if (k.classList.contains('key-home')) {
                        k.dataset.name = HOME
                    } else if (k.classList.contains('key-end')) {
                        k.dataset.name = END
                    } else if (k.classList.contains('key-page-up')) {
                        k.dataset.name = PAGE_UP
                    } else if (k.classList.contains('key-page-down')) {
                        k.dataset.name = PAGE_DOWN
                    } else if (k.classList.contains('key-enter')) {
                        k.dataset.name = ENTER
                    } else if (k.classList.contains('key-clear')) {
                        k.dataset.name = CLEAR
                    } else if (k.classList.contains('key-caps-lock')) {
                        k.dataset.name = CAPS_LOCK
                    } else if (k.classList.contains('key-left')) {
                        k.dataset.name = LEFT
                    } else if (k.classList.contains('key-right')) {
                        k.dataset.name = RIGHT
                    } else if (k.classList.contains('key-up')) {
                        k.dataset.name = UP
                    } else if (k.classList.contains('key-down')) {
                        k.dataset.name = DOWN
                    } else if (k.classList.contains('key-abc')) {
                        k.dataset.name = ABC
                    } else if (k.classList.contains('key-digital')) {
                        k.dataset.name = DIGITAL
                    } else if (k.classList.contains('key-symbol')) {
                        k.dataset.name = SYMBOL
                    } else if (k.classList.contains('key-return')) {
                        k.dataset.name = RETURN
                    } else if (k.classList.contains('key-space')) {
                        k.dataset.name = SPACE
                    } else if (k.classList.contains('key-single-quote')) {
                        k.dataset.name = SINGLE_QUOTE
                    }
                }
            })
        }

        hide() {
            this.el.style.display = 'none'
            this.el.style.zIndex = 0
            this.disableTouch()
        }

        show() {
            this.el.style.display = ''
            this.el.style.zIndex = 99
            this.enableTouch()
        }

        setShift(state, el) {
            if (this.shifted == state) return
            if (state) {
                el.classList.remove('key-shift')
                el.classList.add('key-shift-fill')
                this.el.querySelectorAll('.fime-label').forEach(el => {
                    let label = el.innerHTML
                    if (REG_ALPHABET.test(label)) {
                        el.innerHTML = label.toUpperCase()
                    }
                })
            } else {
                el.classList.remove('key-shift-fill')
                el.classList.add('key-shift')
                this.el.querySelectorAll('.fime-label').forEach(el => {
                    let label = el.innerHTML
                    if (REG_ALPHABET.test(label)) {
                        el.innerHTML = label.toLowerCase()
                    }
                })
            }
            this.shifted = state
        }

        onTap(el) {
            let name = getKeyName(el)
            if (handler && handler(this, 'tap', name)) return
            if (name == SHIFT) {
                this.setShift(!this.shifted, el)
            } else {
                fime.onKey(name)
            }
        }

        onSwipe(el, dir) {
            let name = getKeyName(el) || el.innerHTML
            console.log('onSwipe, name=' + name + ', direction=' + dir)
            if (handler && handler(this, 'swipe' + dir, name)) return
            let handle = false
            if (name == BACKSPACE) {
                fime.onKey(CLEAR)
                handle = true
            }
            if (!handle) {
                let symbol = el.querySelector('.fime-symbol')
                if (symbol && symbol.innerHTML) fime.onKey(symbol.innerHTML)
            }
        }

        onLongPress(el) {
            let name = getKeyName(el)
            if (handler && handler(this, 'longpress', name)) return
            if ([BACKSPACE, SPACE].indexOf(name) >= 0) fime.onKey(name)
        }
    }

    fime.setupKeyboard = function (layouts, h) {
        for (let name in layouts) {
            let el = layouts[name]
            if (typeof el === 'string') le = fime.qs(el)
            if (el) keyboards[name] = new Keyboard(name, el)
        }
        if (typeof h == 'function') handler = h
    }

    fime.useKeyboard = function (layout, mode) {
        for (let key in keyboards) {
            let kbd = keyboards[key]
            if (layout == key) {
                fime.setMode(mode || EN_MODE)
                kbd.show()
            } else {
                kbd.hide()
            }
        }
    }

    w.fime = fime
})(window, document);
