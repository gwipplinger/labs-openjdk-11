/*
 * Copyright (c) 2009, 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package jdk.vm.ci.i386;

import static jdk.vm.ci.code.MemoryBarriers.LOAD_LOAD;
import static jdk.vm.ci.code.MemoryBarriers.LOAD_STORE;
import static jdk.vm.ci.code.MemoryBarriers.STORE_STORE;
import static jdk.vm.ci.code.Register.SPECIAL;

import java.nio.ByteOrder;
import java.util.EnumSet;

import jdk.vm.ci.code.Architecture;
import jdk.vm.ci.code.Register;
import jdk.vm.ci.code.Register.RegisterCategory;
import jdk.vm.ci.code.RegisterArray;
import jdk.vm.ci.meta.JavaKind;
import jdk.vm.ci.meta.PlatformKind;

/**
 * Represents the i386 architecture.
 */
public class I386 extends Architecture {

    public static final RegisterCategory CPU = new RegisterCategory("CPU");

    // @formatter:off

    // General purpose CPU registers
    public static final Register eax = new Register(0, 0, "rax", CPU);
    public static final Register ecx = new Register(1, 1, "rcx", CPU);
    public static final Register edx = new Register(2, 2, "rdx", CPU);
    public static final Register ebx = new Register(3, 3, "rbx", CPU);
    public static final Register esp = new Register(4, 4, "rsp", CPU);
    public static final Register ebp = new Register(5, 5, "rbp", CPU);
    public static final Register esi = new Register(6, 6, "rsi", CPU);
    public static final Register edi = new Register(7, 7, "rdi", CPU);

    public static final Register[] cpuRegisters = {
        eax, ecx, edx, ebx, esp, ebp, esi, edi
    };

    public static final RegisterCategory XMM = new RegisterCategory("XMM");

    // XMM registers
    public static final Register xmm0 = new Register(8, 0, "xmm0", XMM);
    public static final Register xmm1 = new Register(9, 1, "xmm1", XMM);
    public static final Register xmm2 = new Register(10, 2, "xmm2", XMM);
    public static final Register xmm3 = new Register(11, 3, "xmm3", XMM);
    public static final Register xmm4 = new Register(12, 4, "xmm4", XMM);
    public static final Register xmm5 = new Register(13, 5, "xmm5", XMM);
    public static final Register xmm6 = new Register(14, 6, "xmm6", XMM);
    public static final Register xmm7 = new Register(15, 7, "xmm7", XMM);

    public static final Register[] xmmRegistersSSE = {
        xmm0, xmm1, xmm2,  xmm3,  xmm4,  xmm5,  xmm6,  xmm7
    };

    public static final RegisterCategory MASK = new RegisterCategory("MASK", false);

    public static final RegisterArray valueRegistersSSE = new RegisterArray(
        eax,  ecx,  edx,   ebx,   esp,   ebp,   esi,   edi,
        xmm0, xmm1, xmm2,  xmm3,  xmm4,  xmm5,  xmm6,  xmm7
    );

    /**
     * Register used to construct an instruction-relative address.
     */
    public static final Register rip = new Register(16, -1, "rip", SPECIAL);

    public static final RegisterArray allRegisters = new RegisterArray(
        eax,  ecx,  edx,   ebx,   esp,   ebp,   esi,   edi,
        xmm0, xmm1, xmm2,  xmm3,  xmm4,  xmm5,  xmm6,  xmm7,
        rip
    );

    // @formatter:on

    /**
     * Basic set of CPU features mirroring what is returned from the cpuid instruction. See:
     * {@code VM_Version::cpuFeatureFlags}.
     */
    public enum CPUFeature {
        CX8,
        CMOV,
        FXSR,
        HT,
        MMX,
        AMD_3DNOW_PREFETCH,
        SSE,
        SSE2,
        SSE3,
        SSSE3,
        SSE4A,
        SSE4_1,
        SSE4_2,
        POPCNT,
        LZCNT,
        TSC,
        TSCINV,
        AVX,
        AVX2,
        AES,
        ERMS,
        CLMUL,
        BMI1,
        BMI2,
        RTM,
        ADX,
        AVX512F,
        AVX512DQ,
        AVX512PF,
        AVX512ER,
        AVX512CD,
        SHA
    }

    private final EnumSet<CPUFeature> features;

    /**
     * Set of flags to control code emission.
     */
    public enum Flag {
        UseCountLeadingZerosInstruction,
        UseCountTrailingZerosInstruction
    }

    private final EnumSet<Flag> flags;

    private final I386Kind largestKind;

    public I386(EnumSet<CPUFeature> features, EnumSet<Flag> flags) {
        super("I386", I386Kind.QWORD, ByteOrder.LITTLE_ENDIAN, true, allRegisters, LOAD_LOAD | LOAD_STORE | STORE_STORE, 1, 4);
        this.features = features;
        this.flags = flags;
        assert features.contains(CPUFeature.SSE2) : "minimum config for i386";
        largestKind = I386Kind.V128_QWORD;
    }

    public EnumSet<CPUFeature> getFeatures() {
        return features;
    }

    public EnumSet<Flag> getFlags() {
        return flags;
    }

    @Override
    public RegisterArray getAvailableValueRegisters() {
        return valueRegistersSSE;
    }

    @Override
    public PlatformKind getPlatformKind(JavaKind javaKind) {
        switch (javaKind) {
            case Boolean:
            case Byte:
                return I386Kind.BYTE;
            case Short:
            case Char:
                return I386Kind.WORD;
            case Int:
            case Object:
                return I386Kind.DWORD;
            case Long:
                return I386Kind.QWORD;
            case Float:
                return I386Kind.SINGLE;
            case Double:
                return I386Kind.DOUBLE;
            default:
                return null;
        }
    }

    @Override
    public boolean canStoreValue(RegisterCategory category, PlatformKind platformKind) {
        I386Kind kind = (I386Kind) platformKind;
        if (kind.isInteger()) {
            return category.equals(CPU);
        } else if (kind.isXMM()) {
            return category.equals(XMM);
        } else {
            assert kind.isMask();
            return category.equals(MASK);
        }
    }

    @Override
    public I386Kind getLargestStorableKind(RegisterCategory category) {
        if (category.equals(CPU)) {
            return I386Kind.DWORD;
        } else if (category.equals(XMM)) {
            return largestKind;
        } else if (category.equals(MASK)) {
            return I386Kind.MASK32;
        } else {
            return null;
        }
    }
}
