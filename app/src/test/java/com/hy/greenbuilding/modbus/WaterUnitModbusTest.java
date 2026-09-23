package com.hy.greenbuilding.modbus;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WaterUnitModbusTest {
    @Test
    public void registerMapUsesConfirmedZeroBasedAddresses() {
        assertRegister("P01", 1001, ModbusFunction.READ_HOLDING_REGISTERS, true);
        assertRegister("P129", 1129, ModbusFunction.READ_HOLDING_REGISTERS, true);
        assertRegister("P130", 1400, ModbusFunction.READ_HOLDING_REGISTERS, true);
        assertRegister("P169", 1439, ModbusFunction.READ_HOLDING_REGISTERS, true);
        assertRegister("B01", 2121, ModbusFunction.READ_INPUT_REGISTERS, false);
        assertRegister("D12", 2162, ModbusFunction.READ_INPUT_REGISTERS, false);
        assertRegister("Y23", 2193, ModbusFunction.READ_INPUT_REGISTERS, false);
    }

    @Test
    public void readInputFrameKeepsCatalogAddressAndUsesModbusCrcOrder() {
        byte[] frame = ModbusRtuFrame.readRegisters(WaterUnitRegisterMap.SLAVE_ID,
                ModbusFunction.READ_INPUT_REGISTERS, 2121, 30);

        assertEquals(8, frame.length);
        assertEquals(0x01, frame[0] & 0xFF);
        assertEquals(0x04, frame[1] & 0xFF);
        assertEquals(0x08, frame[2] & 0xFF);
        assertEquals(0x49, frame[3] & 0xFF);
        assertEquals(0x00, frame[4] & 0xFF);
        assertEquals(0x1E, frame[5] & 0xFF);
        assertTrue(ModbusRtuFrame.hasValidCrc(frame));
    }

    private static void assertRegister(String code, int address, int function, boolean writable) {
        WaterUnitRegisterMap.Register register = WaterUnitRegisterMap.resolve(code);
        assertNotNull(register);
        assertEquals(address, register.address);
        assertEquals(function, register.readFunction);
        assertEquals(writable, register.writable);
    }
}
