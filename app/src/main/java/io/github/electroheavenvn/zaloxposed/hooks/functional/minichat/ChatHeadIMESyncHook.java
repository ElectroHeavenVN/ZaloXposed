package io.github.electroheavenvn.zaloxposed.hooks.functional.minichat;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.WindowInsetsAnimation;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import org.luckypray.dexkit.query.FindField;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.FieldMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.utilities.Config;
import io.github.electroheavenvn.zaloxposed.utilities.DexKitCache;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;

public class ChatHeadIMESyncHook extends BaseHook
{
    private static Class<?> hostFrameLayout;
    private static Class<?> chatFrameLayout;
    private static Class<?> upArrowLayout;
    private static Method getPointTo;
    private static Field kbMetricsHeight;
    private static final Map<View, State> states = Collections.synchronizedMap(new WeakHashMap<>());

    private record Geometry(int keyboardHeight, int visibleBottom) { }

    private static final class State
    {
        final WeakReference<View> host;
        final Set<WindowInsetsAnimation> animations = Collections.newSetFromMap(new IdentityHashMap<>());
        final Set<WindowInsetsAnimation> started = Collections.newSetFromMap(new IdentityHashMap<>());
        WindowInsets pending;
        int imeBottom, navBottom, lastWidth, lastHeight, baselineWidth, baselineHeight, baselineY;
        long epoch;
        boolean known, queued, wakeQueued;
        long wakeUntil, wakeGeneration;
        ViewTreeObserver observer;
        ViewTreeObserver.OnPreDrawListener preDraw;
        ViewTreeObserver.OnGlobalLayoutListener globalLayout;
        final Set<View> frames = Collections.newSetFromMap(new WeakHashMap<>());

        State(View host) { this.host = new WeakReference<>(host); }
    }

    @SuppressWarnings("NullableProblems")
    @SuppressLint("UseRequiresApi")
    @TargetApi(Build.VERSION_CODES.R)
    private static final class Callback extends WindowInsetsAnimation.Callback
    {
        private final State state;
        private final WindowInsetsAnimation.Callback nativeCallback;

        Callback(State state, WindowInsetsAnimation.Callback nativeCallback)
        {
            super(nativeCallback == null ? DISPATCH_MODE_CONTINUE_ON_SUBTREE : nativeCallback.getDispatchMode());
            this.state = state;
            this.nativeCallback = nativeCallback;
        }

        @Override
        public void onPrepare(WindowInsetsAnimation animation)
        {
            if ((animation.getTypeMask() & WindowInsets.Type.ime()) != 0)
            {
                state.animations.add(animation);
                if (Build.VERSION.SDK_INT == 30)
                {
                    View host = state.host.get();
                    long epoch = state.epoch;
                    if (host != null)
                    {
                        host.postDelayed(() ->
                        {
                            View current = state.host.get();
                            if (current == null || state.epoch != epoch || states.get(current) != state)
                                return;
                            if (state.started.contains(animation) || !state.animations.remove(animation))
                                return;
                            if (state.animations.isEmpty())
                                sample(state, current.getRootWindowInsets());
                        }, 250);
                    }
                }
            }
            if (nativeCallback != null)
                nativeCallback.onPrepare(animation);
        }

        @Override
        public WindowInsetsAnimation.Bounds onStart(WindowInsetsAnimation animation, WindowInsetsAnimation.Bounds bounds)
        {
            if ((animation.getTypeMask() & WindowInsets.Type.ime()) != 0)
                state.started.add(animation);
            return nativeCallback == null ? bounds : nativeCallback.onStart(animation, bounds);
        }

        @Override
        public WindowInsets onProgress(WindowInsets insets, List<WindowInsetsAnimation> running)
        {
            for (WindowInsetsAnimation animation : running)
                if ((animation.getTypeMask() & WindowInsets.Type.ime()) != 0)
                    state.animations.add(animation);
            sample(state, insets);
            return nativeCallback == null ? insets : nativeCallback.onProgress(insets, running);
        }

        @Override
        public void onEnd(WindowInsetsAnimation animation)
        {
            if (nativeCallback != null)
                nativeCallback.onEnd(animation);
            state.animations.remove(animation);
            state.started.remove(animation);
            View host = state.host.get();
            if (!state.animations.isEmpty() || host == null)
                return;
            WindowInsets current = host.getRootWindowInsets();
            sample(state, current == null ? state.pending : current);
        }
    }

    @SuppressLint({"WrongConstant", "PrivateApi"})
    @Override
    public void hook() throws ClassNotFoundException, NoSuchMethodException
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
            return;
        hostFrameLayout = Class.forName("com.zing.zalo.comm.chathead.minichat.ui.container.HostFrameLayout", false, classLoader);
        chatFrameLayout = Class.forName("com.zing.zalo.ui.chat.ChatFrameLayout", false, classLoader);
        upArrowLayout = Class.forName("com.zing.zalo.comm.chathead.minichat.ui.UpArrowLayout", false, classLoader);
        getPointTo = upArrowLayout.getDeclaredMethod("getPointTo");
        if (getPointTo.getReturnType() != Point.class)
            return;
        getPointTo.setAccessible(true);
        Method getSoftKeyboardHeight = DexKitCache.GetMethod("ChatHeadIMESync_KeyboardUtils_GetSoftKeyboardHeight", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("void")
                .paramCount(3)
                .paramTypes("android.view.View", null, "int")
                .addUsingString("getSoftKeyboardHeight", StringMatchType.Contains)
                .addUsingString("KeyboardUtils", StringMatchType.Equals)
                .addUsingString("KeyboardUtils#getSoftKeyboardHeight", StringMatchType.Contains)
            )), classLoader);
        if (getSoftKeyboardHeight == null)
            return;
        // Fuck Horizontal Class Merging
        Class<?> keyboardMetrics = getSoftKeyboardHeight.getParameterTypes()[1];
        kbMetricsHeight = DexKitCache.GetFieldRaw("ChatHeadIMESync_KeyboardMetrics_Height", classLoader);
        try
        {
            // ctor(int size) are from LruCache, not KeyboardMetrics, may disappear in the future
            Object keyboardMetricsInst = keyboardMetrics.getConstructor(int.class).newInstance(6767);
            for (Field f : keyboardMetrics.getDeclaredFields())
            {
                if (f.getType() != int.class)
                    continue;
                boolean accessible = f.isAccessible();
                f.setAccessible(true);
                Integer value = (Integer)f.get(keyboardMetricsInst);
                if (value == null || 6767 != value)
                {
                    f.setAccessible(accessible);
                    continue;
                }
                kbMetricsHeight = f;
                break;
            }
        }
        catch (Exception e)
        {
            Logger.e(e);
            return;
        }
        if (kbMetricsHeight == null)
            return;
        DexKitCache.SetFieldRaw("ChatHeadIMESync_KeyboardMetrics_Height", kbMetricsHeight);
        Field chatFrameLayoutKeyboardMetrics = DexKitCache.GetField("ChatHeadIMESync_ChatFrameLayout_KeyboardMetrics", () -> bridge.findField(FindField.create()
            .matcher(FieldMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .declaredClass(chatFrameLayout).type(keyboardMetrics)
            )), classLoader);
        if (chatFrameLayoutKeyboardMetrics == null)
            return;
        Field visibleDisplayFrame = Arrays.stream(keyboardMetrics.getFields())
            .filter(f -> f.getType() == Object.class || f.getType() == Rect.class)
            .findFirst()
            .orElse(null);
        if (visibleDisplayFrame == null)
            return;


        Method setWindowInsetsAnimationCallback = View.class.getDeclaredMethod("setWindowInsetsAnimationCallback", WindowInsetsAnimation.Callback.class);
        Logger.h(setWindowInsetsAnimationCallback);
        module.hook(setWindowInsetsAnimationCallback).intercept(chain ->
        {
            if (!Config.getEnableChatHead())
                return chain.proceed();
            if (chain.getThisObject() instanceof View host)
            {
                if (hostFrameLayout.isInstance(host) && !(chain.getArg(0) instanceof Callback))
                    return chain.proceed(new Object[]{new Callback(state(host), (WindowInsetsAnimation.Callback)chain.getArg(0))});
            }
            return chain.proceed();
        });
        Method dispatchApplyWindowInsets = View.class.getDeclaredMethod("dispatchApplyWindowInsets", WindowInsets.class);
        Logger.h(dispatchApplyWindowInsets);
        module.hook(dispatchApplyWindowInsets).intercept(chain ->
        {
            Object result = chain.proceed();
            if (!Config.getEnableChatHead())
                return result;
            if (chain.getThisObject() instanceof View host && hostFrameLayout.isInstance(host))
            {
                State state = state(host);
                WindowInsets raw = (WindowInsets)chain.getArg(0);
                state.pending = raw;
                if (state.animations.isEmpty())
                    sample(state, raw);
            }
            return result;
        });
        Method onAttachedToWindow = View.class.getDeclaredMethod("onAttachedToWindow");
        onAttachedToWindow.setAccessible(true);
        Logger.h(onAttachedToWindow);
        module.hook(onAttachedToWindow).intercept(chain ->
        {
            Object result = chain.proceed();
            if (!Config.getEnableChatHead())
                return result;
            if (chain.getThisObject() instanceof View host && hostFrameLayout.isInstance(host))
            {
                state(host);
                host.requestApplyInsets();
            }
            return result;
        });
        Method onDetachedFromWindow = View.class.getDeclaredMethod("onDetachedFromWindow");
        onDetachedFromWindow.setAccessible(true);
        Logger.h(onDetachedFromWindow);
        module.hook(onDetachedFromWindow).intercept(chain ->
        {
            Object result = chain.proceed();
            if (!Config.getEnableChatHead())
                return result;
            if (chain.getThisObject() instanceof View host && hostFrameLayout.isInstance(host))
            {
                State old = states.get(host);
                if (old == null)
                    return result;
                old.epoch++;
                old.animations.clear();
                old.started.clear();
                old.known = false;
                old.queued = false;
                old.pending = null;
                old.baselineHeight = 0;
                old.baselineWidth = 0;
                stopObservation(old);
            }
            return result;
        });
        Logger.h(getSoftKeyboardHeight);
        module.hook(getSoftKeyboardHeight).intercept(chain ->
        {
            Object result = chain.proceed();
            if (!Config.getEnableChatHead())
                return result;
            Object object = chain.getArg(0);
            Object model = chain.getArg(1);
            if (!(object instanceof View frame) || !chatFrameLayout.isInstance(object))
                return result;
            View host = hostOf(frame);
            if (host == null || !frame.isAttachedToWindow() || chatFrameLayoutKeyboardMetrics.get(frame) != model)
                return result;
            if (!(visibleDisplayFrame.get(model) instanceof Rect rect))
                return result;
            State state = state(host);
            state.frames.add(frame);
            if (state.animations.isEmpty())
                sample(state, host.getRootWindowInsets());
            if (!state.known && state.animations.isEmpty())
                sample(state, host.getRootWindowInsets());
            if (!state.known)
                return result;
            try
            {
                int height = host.getHeight();
                if (height <= 0 || frame.getMeasuredHeight() <= 0)
                    return result;
                int frameTop = localTop(frame, host);
                if (frame.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
                {
                    View current = frame;
                    while (current != host)
                    {
                        if (upArrowLayout.isInstance(current))
                        {
                            Point point = (Point)getPointTo.invoke(current);
                            if (point != null)
                                frameTop = Math.max(frameTop, point.y);
                            break;
                        }
                        ViewParent parent = current.getParent();
                        if (!(parent instanceof View view))
                            break;
                        current = view;
                    }
                }
                int minimum = minimumContent(frame);
                int remainingIme = state.imeBottom;
                if (state.baselineHeight > 0 && state.baselineWidth == host.getWidth())
                {
                    int[] location = new int[2];
                    host.getLocationOnScreen(location);
                    remainingIme = remainingOcclusion(height, location[1], state.baselineHeight, state.baselineY, state.imeBottom);
                }
                Geometry geometry = geometry(height, frameTop, minimum, remainingIme, state.navBottom);
                kbMetricsHeight.setInt(model, geometry.keyboardHeight);
                if (state.imeBottom > 0)
                    rect.bottom = geometry.visibleBottom;
            }
            catch (Throwable t) { Logger.e(t); }
            return result;
        });
        Method getRealOffsetBottom = chatFrameLayout.getDeclaredMethod("getRealOffsetBottom");
        getRealOffsetBottom.setAccessible(true);
        Logger.h(getRealOffsetBottom);
        module.hook(getRealOffsetBottom).intercept(chain ->
        {
            Object result = chain.proceed();
            if (!Config.getEnableChatHead())
                return result;
            if (chain.getThisObject() instanceof View view && chatFrameLayout.isInstance(view))
            {
                View host = hostOf(view);
                State state = null;
                if (host != null)
                    state = states.get(host);
                if (state != null && state.known && state.imeBottom > 0)
                    return 0;
            }
            return result;
        });
        for (Method inputMethodManagerMethod : InputMethodManager.class.getDeclaredMethods())
        {
            if (!Modifier.isPublic(inputMethodManagerMethod.getModifiers()))
                continue;
            else if (Modifier.isAbstract(inputMethodManagerMethod.getModifiers()))
                continue;
            String name = inputMethodManagerMethod.getName();
            Class<?>[] params = inputMethodManagerMethod.getParameterTypes();
            boolean show = name.equals("showSoftInput") && params.length >= 2 && params[0] == View.class;
            boolean hide = name.equals("hideSoftInputFromWindow") && params.length >= 2 && params[0] == IBinder.class;
            if (!show && !hide)
                continue;
            Logger.h(inputMethodManagerMethod);
            module.hook(inputMethodManagerMethod).intercept(chain ->
            {
                if (!Config.getEnableChatHead())
                    return chain.proceed();
                State affected = null;
                if (show && chain.getArg(0) instanceof View)
                {
                    View host = hostOf((View)chain.getArg(0));
                    if (host != null)
                        affected = state(host);
                }
                else if (hide && chain.getArg(0) != null)
                {
                    for (State item : new ArrayList<>(states.values()))
                    {
                        View host = item.host.get();
                        if (host == null || !chain.getArg(0).equals(host.getWindowToken()))
                            continue;
                        affected = item;
                        break;
                    }
                }
                Object result = chain.proceed();
                if (affected != null)
                    wake(affected);
                return result;
            });
        }
        try
        {
            Class<?> insetsController = Class.forName("android.view.InsetsController");
            for (Method method : insetsController.getDeclaredMethods())
            {
                Class<?>[] parameters = method.getParameterTypes();
                if (!method.getName().equals("onStateChanged") || parameters.length != 1
                    || !parameters[0].getName().equals("android.view.InsetsState")
                    || Modifier.isAbstract(method.getModifiers()))
                    continue;
                Logger.h(method);
                module.hook(method).intercept(chain ->
                {
                    Object result = chain.proceed();
                    if (!Config.getEnableChatHead())
                        return result;
                    for (State item : new ArrayList<>(states.values()))
                    {
                        View host = item.host.get();
                        if (host != null && host.isAttachedToWindow() && host.isShown() && host.hasWindowFocus())
                            host.post(() -> wake(item));
                    }
                    return result;
                });
            }
        }
        catch (ClassNotFoundException ignored) { }
        Class<?> manager = Class.forName("android.view.WindowManagerImpl");
        for (String methodName : new String[] { "addView", "updateViewLayout" })
        {
            Method method = manager.getDeclaredMethod(methodName, View.class, ViewGroup.LayoutParams.class);
            method.setAccessible(true);
            Logger.h(method);
            module.hook(method).intercept(chain ->
            {
                if (!Config.getEnableChatHead())
                    return chain.proceed();
                if (chain.getArg(0) instanceof View && hostFrameLayout.isInstance(chain.getArg(0))
                    && chain.getArg(1) instanceof WindowManager.LayoutParams params)
                    params.softInputMode = (params.softInputMode & ~WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST) | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING;
                return chain.proceed();
            });
        }
    }

    @SuppressLint("UseRequiresApi")
    @TargetApi(Build.VERSION_CODES.R)
    private static State state(View host)
    {
        synchronized (states)
        {
            State state = states.get(host);
            if (state != null)
            {
                observe(state, host);
                return state;
            }
            state = new State(host);
            states.put(host, state);
            host.setWindowInsetsAnimationCallback(new Callback(state, null));
            observe(state, host);
            return state;
        }
    }

    @SuppressLint("UseRequiresApi")
    @TargetApi(Build.VERSION_CODES.R)
    private static void sample(State state, WindowInsets insets)
    {
        View host = state.host.get();
        if (host == null || insets == null || !host.isAttachedToWindow())
            return;
        int ime = insets.getInsets(WindowInsets.Type.ime()).bottom;
        boolean visible = insets.isVisible(WindowInsets.Type.ime());
        if (!visible && state.animations.isEmpty())
            ime = 0;
        int nav = insets.getInsets(WindowInsets.Type.navigationBars()).bottom;
        boolean boundsChanged = state.lastWidth != host.getWidth() || state.lastHeight != host.getHeight();
        int oldNav = state.navBottom;
        if (ime == 0 || !state.known || boundsChanged)
            state.navBottom = nav;
        boolean changed = !state.known || state.imeBottom != ime || state.navBottom != oldNav || boundsChanged;
        state.known = true;
        state.imeBottom = Math.max(0, ime);
        state.lastWidth = host.getWidth();
        state.lastHeight = host.getHeight();
        if (ime == 0 || state.baselineHeight <= 0 || state.baselineWidth != host.getWidth())
        {
            state.baselineHeight = host.getHeight();
            state.baselineWidth = host.getWidth();
            int[] location = new int[2];
            host.getLocationOnScreen(location);
            state.baselineY = location[1];
        }
        if (changed)
            requestLayout(state);
    }

    private static void requestLayout(State state)
    {
        View host = state.host.get();
        if (host == null || !host.isAttachedToWindow() || state.queued)
            return;
        state.queued = true;
        long epoch = state.epoch;
        host.postOnAnimation(() ->
        {
            state.queued = false;
            View current = state.host.get();
            if (current == null || !current.isAttachedToWindow() || state.epoch != epoch || states.get(current) != state)
                return;
            collectFrames(state, current);
            for (View frame : new ArrayList<>(state.frames))
            {
                if (frame.isAttachedToWindow() && hostOf(frame) == current)
                    frame.requestLayout();
            }
            current.requestLayout();
            current.invalidate();
        });
    }

    private static View hostOf(View view)
    {
        View current = view;
        while (current != null)
        {
            if (hostFrameLayout.isInstance(current))
                return current;
            ViewParent parent = current.getParent();
            if (!(parent instanceof View))
                break;
            current = (View)parent;
        }
        return null;
    }

    private static void observe(State state, View host)
    {
        if (!host.isAttachedToWindow())
            return;
        ViewTreeObserver observer = host.getViewTreeObserver();
        if (state.observer == observer && observer.isAlive())
            return;
        stopObservation(state);
        state.observer = observer;
        state.preDraw = () ->
        {
            if (state.animations.isEmpty())
                sample(state, host.getRootWindowInsets());
            return true;
        };
        state.globalLayout = () ->
        {
            if (state.animations.isEmpty())
                sample(state, host.getRootWindowInsets());
        };
        observer.addOnPreDrawListener(state.preDraw);
        observer.addOnGlobalLayoutListener(state.globalLayout);
        collectFrames(state, host);
    }

    private static void stopObservation(State state)
    {
        if (state.observer != null && state.observer.isAlive())
        {
            if (state.preDraw != null)
                state.observer.removeOnPreDrawListener(state.preDraw);
            if (state.globalLayout != null)
                state.observer.removeOnGlobalLayoutListener(state.globalLayout);
        }
        state.observer = null;
        state.preDraw = null;
        state.globalLayout = null;
        state.wakeGeneration++;
        state.wakeQueued = false;
        state.wakeUntil = 0;
        state.frames.clear();
    }

    private static void collectFrames(State state, View root)
    {
        ArrayList<View> queue = new ArrayList<>();
        queue.add(root);
        for (int i = 0; i < queue.size(); i++)
        {
            View view = queue.get(i);
            if (chatFrameLayout.isInstance(view))
                state.frames.add(view);
            if (!(view instanceof ViewGroup group))
                continue;
            for (int j = 0; j < group.getChildCount(); j++)
                queue.add(group.getChildAt(j));
        }
    }

    private static int localTop(View child, View ancestor)
    {
        int y = 0;
        View current = child;
        while (current != ancestor)
        {
            y += current.getTop();
            ViewParent parent = current.getParent();
            if (!(parent instanceof View))
                return 0;
            current = (View)parent;
        }
        return Math.max(0, y);
    }

    private static int minimumContent(View frame)
    {
        int header = 0, input = 0;
        ArrayList<View> queue = new ArrayList<>();
        queue.add(frame);
        for (int i = 0; i < queue.size() && i < 180; i++)
        {
            View view = queue.get(i);
            String name = view.getClass().getName();
            if (name.endsWith(".ZdsActionBar"))
                header = Math.max(header, view.getMeasuredHeight());
            if (name.endsWith(".ChatInputBar"))
                input = Math.max(input, view.getMeasuredHeight());
            if (!(view instanceof ViewGroup group))
                continue;
            for (int j = 0; j < group.getChildCount(); j++)
            {
                if (group.getChildAt(j).getVisibility() != View.GONE)
                    queue.add(group.getChildAt(j));
            }
        }
        int fallback = Math.round(frame.getResources().getDisplayMetrics().density * 96);
        return Math.max(fallback, header + input);
    }

    private static int remainingOcclusion(int currentHeight, int currentY, int baselineHeight, int baselineY, int imeBottom)
    {
        if (imeBottom <= 0)
            return 0;
        long top = (long)baselineY + baselineHeight - imeBottom;
        long overlap = (long)currentY + currentHeight - top;
        return (int)Math.max(0, Math.min(currentHeight, overlap));
    }

    private static Geometry geometry(int hostHeight, int chatTop, int minContent, int imeBottom, int navBottom)
    {
        int top = Math.max(0, Math.min(chatTop, hostHeight));
        int minimum = Math.max(0, Math.min(minContent, hostHeight - top));
        int visibleBottom = Math.max(top + minimum, Math.min(hostHeight, hostHeight - Math.max(0, imeBottom)));
        int maxHeight = Math.max(0, hostHeight - top - minimum);
        int keyboard = Math.max(0, Math.min(maxHeight, Math.max(0, imeBottom) - Math.max(0, navBottom)));
        return new Geometry(keyboard, visibleBottom);
    }

    private void wake(State state)
    {
        View host = state.host.get();
        if (host == null || !host.isAttachedToWindow())
            return;
        if (state.wakeQueued)
            return;
        state.wakeUntil = SystemClock.uptimeMillis() + 1500;
        state.wakeQueued = true;
        long epoch = state.epoch, generation = state.wakeGeneration;
        Runnable tick = new Runnable()
        {
            int lastIme = state.imeBottom;
            int stableFrames;
            boolean sawChange;

            public void run()
            {
                View current = state.host.get();
                if (current == null || !current.isAttachedToWindow())
                {
                    state.wakeQueued = false;
                    return;
                }
                if (state.epoch != epoch || state.wakeGeneration != generation || states.get(current) != state)
                {
                    state.wakeQueued = false;
                    return;
                }
                current.requestApplyInsets();
                if (state.animations.isEmpty())
                    sample(state, current.getRootWindowInsets());
                requestLayout(state);
                if (state.imeBottom != lastIme)
                {
                    sawChange = true;
                    stableFrames = 0;
                    lastIme = state.imeBottom;
                }
                else
                    stableFrames++;
                boolean settled = sawChange && stableFrames >= 3 && state.animations.isEmpty();
                if (!settled && SystemClock.uptimeMillis() < state.wakeUntil)
                    current.postOnAnimation(this);
                else
                    state.wakeQueued = false;
            }
        };
        host.postOnAnimation(tick);
    }
}