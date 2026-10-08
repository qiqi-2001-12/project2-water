package com.hy.greenbuilding.modbus;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        assertRegister("S01", 911, ModbusFunction.READ_HOLDING_REGISTERS, true);
        assertRegister("S02", 912, ModbusFunction.READ_HOLDING_REGISTERS, true);
        assertRegister("S30", 940, ModbusFunction.READ_HOLDING_REGISTERS, true);
        assertRegister("S63", 973, ModbusFunction.READ_HOLDING_REGISTERS, true);
        assertRegister("S64", 974, ModbusFunction.READ_HOLDING_REGISTERS, true);
        assertRegister("F01", 2115, ModbusFunction.READ_INPUT_REGISTERS, false);
        assertRegister("F08", 2100, ModbusFunction.READ_INPUT_REGISTERS, false);
        assertRegister("A01", 2114, ModbusFunction.READ_INPUT_REGISTERS, false);
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

    @Test
    public void waterUnitControlUsesConfirmedS01AndS02Registers() {
        assertEquals(911, WaterUnitControl.POWER_REGISTER);
        assertEquals(912, WaterUnitControl.MODE_REGISTER);
        assertEquals(0, WaterUnitControl.MODE_COOLING);
        assertEquals(1, WaterUnitControl.MODE_HEATING);
    }

    @Test
    public void settingsAreReadInIndependentSmallBlocks() {
        List<WaterUnitRegisterMap.RegisterBlock> blocks =
                WaterUnitRegisterMap.getSettingReadBlocks();

        assertBlock(blocks.get(6), 1121, 5);
        assertBlock(blocks.get(7), 1126, 4);
        assertBlock(blocks.get(8), 1400, 20);
        assertBlock(blocks.get(9), 1420, 20);
        assertBlock(blocks.get(11), 973, 2);
    }

    @Test
    public void systemAndFaultRegistersAreIncludedInStatusPolling() {
        List<WaterUnitRegisterMap.RegisterBlock> blocks =
                WaterUnitRegisterMap.getStatusPollBlocks();

        assertBlock(blocks.get(0), 2100, 21);
        assertEquals(2115, WaterUnitRegisterMap.getFaultInformationAddress(1));
        assertEquals(2120, WaterUnitRegisterMap.getFaultInformationAddress(5));
        assertEquals(2109, WaterUnitRegisterMap.getFaultInformationAddress(6));
        assertEquals(2110, WaterUnitRegisterMap.getFaultInformationAddress(7));
        assertEquals(2100, WaterUnitRegisterMap.getFaultInformationAddress(8));
    }

    @Test
    public void faultDecoderUsesSpecificNamesInHighBitFirstOrder() {
        Map<Integer, Integer> values = new HashMap<>();
        values.put(WaterUnitRegisterMap.STATUS_FAULT_INFO_1_ADDRESS, 0x8001);

        List<WaterUnitFaultDecoder.Fault> faults = WaterUnitFaultDecoder.decode(values);

        assertEquals(2, faults.size());
        assertEquals(15, faults.get(0).bit);
        assertEquals("系统2防冻感温故障", faults.get(0).name);
        assertEquals(0, faults.get(1).bit);
        assertEquals("进水感温故障", faults.get(1).name);
    }

    @Test
    public void systemStatusDecoderUsesHighBitFirstOrder() {
        assertEquals("直流风机1故障 / 运行", WaterUnitSystemStatusDecoder.decode(0x0804));
    }

    private static void assertRegister(String code, int address, int function, boolean writable) {
        WaterUnitRegisterMap.Register register = WaterUnitRegisterMap.resolve(code);
        assertNotNull(register);
        assertEquals(address, register.address);
        assertEquals(function, register.readFunction);
        assertEquals(writable, register.writable);
    }

    private static void assertBlock(WaterUnitRegisterMap.RegisterBlock block, int address,
                                    int quantity) {
        assertEquals(address, block.startAddress);
        assertEquals(quantity, block.quantity);
    }
}
