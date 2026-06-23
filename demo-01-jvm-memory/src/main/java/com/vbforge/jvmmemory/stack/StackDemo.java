package com.vbforge.jvmmemory.stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Q2 — What is the stack for, and what is stored in it?
 *
 * KEY POINTS this demo makes visible:
 *
 *  1. Every method call pushes a new FRAME onto the current thread's stack.
 *  2. Local PRIMITIVES (int, long, boolean…) live entirely on the stack.
 *  3. Local REFERENCES also live on the stack — but the object they point
 *     to lives on the heap. The variable holds only an address (pointer).
 *  4. When a method returns the frame is POPPED — primitives are gone
 *     instantly, no GC required.
 *  5. Unbounded recursion → StackOverflowError (the stack has a fixed size,
 *     configurable with -Xss, default ~512 KB – 1 MB per thread).
 */
@Component
public class StackDemo {

    private static final Logger log = LoggerFactory.getLogger(StackDemo.class);

    /**
     * Demonstrates a simple call chain so you can see frame depth in the log.
     * Watch the log output — each method logs its own frame number and the
     * local variable values that live inside that frame on the stack.
     */
    public String runCallChainDemo() {
        log.debug("=== STACK DEMO: call chain ===");
        int result = frameA(10);
        return "Result from call chain: " + result;
    }

    // JUNIOR NOTE: 'a' is a local primitive — it lives on THIS frame's stack slot.
    // When frameA returns, 'a' and 'intermediate' simply cease to exist.
    // No GC is ever involved.
    private int frameA(int a) {
        log.debug("[frameA] local primitive a={} lives on stack frame #A", a);
        int intermediate = a * 2;   // another stack slot
        return frameB(intermediate);
    }

    private int frameB(int b) {
        log.debug("[frameB] local primitive b={} lives on stack frame #B", b);

        // 'message' is a REFERENCE — it occupies one stack slot (a pointer / address).
        // The actual String object lives on the HEAP (String pool area).
        String message = "hello from frameB";
        log.debug("[frameB] reference 'message' is on the stack; the String object '{}' is on the heap", message);

        return frameC(b + 1);
    }

    private int frameC(int c) {
        log.debug("[frameC] local primitive c={} lives on stack frame #C — deepest frame in chain", c);
        return c * 3;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // StackOverflowError demo
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Controlled StackOverflowError.
     *
     * Each recursive call pushes a new frame. Because recurse() never returns
     * (it always calls itself) the stack fills up and the JVM throws
     * StackOverflowError — NOT OutOfMemoryError.
     *
     * JUNIOR NOTE: StackOverflowError is an Error, not an Exception.
     * Catching it is legal but almost never the right thing to do.
     */
    public String runStackOverflowDemo() {
        log.debug("=== STACK DEMO: deliberate StackOverflowError ===");
        try {
            recurse(0);
            return "Should never reach here";
        } catch (StackOverflowError e) {
            String msg = "StackOverflowError caught after deep recursion -> stack exhausted";
            log.warn(msg);
            return msg;
        }
    }

    // JUNIOR NOTE: notice there is NO base case — this recurses forever.
    // In real code a missing base case in recursion is the #1 cause of SOE.
    private void recurse(int depth) {
        // Each call adds a frame with local variable 'depth' on the stack.
        // Default thread stack size is ~512KB–1MB; each frame is ~hundreds of bytes.
        recurse(depth + 1);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Reference vs primitive on the stack
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Illustrates the exact difference between a primitive slot and a
     * reference slot on the stack, and proves that Java is ALWAYS pass-by-value.
     */
    public String runPassByValueDemo() {
        log.debug("=== STACK DEMO: pass-by-value ===");

        // Primitive — passing a COPY of the value
        int x = 5;
        tryToModifyPrimitive(x);
        log.debug("After tryToModifyPrimitive(x): x={} (unchanged -> copy was passed)", x);

        // Object reference — passing a COPY of the REFERENCE (address)
        // The method CAN mutate the object because both caller and callee
        // hold a reference to the SAME heap object.
        // But reassigning the parameter inside the method does NOT affect the caller.
        int[] arr = {1, 2, 3};
        tryToModifyArray(arr);
        log.debug("After tryToModifyArray(arr): arr[0]={} (mutated via shared reference)", arr[0]);

        System.out.println("arr = " + Arrays.toString(arr)); //[99, 2, 3]

        return String.format("x=%d (unchanged), arr[0]=%d (mutated via reference) arr now: [arr[0]=%d, arr[1]=%d, arr[2]=%d]", x, arr[0], arr[0], arr[1], arr[2]);
    }

    private void tryToModifyPrimitive(int value) {
        // JUNIOR NOTE: 'value' is a separate stack slot — a copy of x.
        // Changing it here has zero effect on the caller's 'x'.
        value = 999;
        log.debug("[tryToModifyPrimitive] set value={} but caller's x is unaffected", value);
    }

    private void tryToModifyArray(int[] ref) {
        // JUNIOR NOTE: 'ref' is a copy of the REFERENCE (the address).
        // ref[0] = 99 follows the address to the heap and mutates the object there.
        // The caller holds the same address, so it sees the mutation.
        ref[0] = 99;
        log.debug("[tryToModifyArray] mutated ref[0]={} -> same heap object", ref[0]);
    }
}
