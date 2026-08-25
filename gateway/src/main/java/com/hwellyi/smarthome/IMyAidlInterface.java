package com.hwellyi.smarthome;

/**
 * This file is auto-generated.  DO NOT MODIFY.
 */
public interface IMyAidlInterface extends android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends android.os.Binder implements com.hwellyi.smarthome.IMyAidlInterface {
        private static final java.lang.String DESCRIPTOR = "com.hwellyi.smarthome.IMyAidlInterface";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an com.hwellyi.smarthome.IMyAidlInterface interface,
         * generating a proxy if needed.
         */
        public static com.hwellyi.smarthome.IMyAidlInterface asInterface(android.os.IBinder obj) {
            if ((obj == null)) {
                return null;
            }
            android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (((iin != null) && (iin instanceof com.hwellyi.smarthome.IMyAidlInterface))) {
                return ((com.hwellyi.smarthome.IMyAidlInterface) iin);
            }
            return new com.hwellyi.smarthome.IMyAidlInterface.Stub.Proxy(obj);
        }

        @Override
        public android.os.IBinder asBinder() {
            return this;
        }

        @Override
        public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException {
            java.lang.String descriptor = DESCRIPTOR;
            switch (code) {
                case INTERFACE_TRANSACTION: {
                    reply.writeString(descriptor);
                    return true;
                }
                case TRANSACTION_onPrintLogToJni: {
                    data.enforceInterface(descriptor);
                    java.lang.String _arg0;
                    _arg0 = data.readString();
                    this.onPrintLogToJni(_arg0);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_onGetNetWorkStatus: {
                    data.enforceInterface(descriptor);
                    boolean _result = this.onGetNetWorkStatus();
                    reply.writeNoException();
                    reply.writeInt(((_result) ? (1) : (0)));
                    return true;
                }
                case TRANSACTION_onGetSerial: {
                    data.enforceInterface(descriptor);
                    java.lang.String _result = this.onGetSerial();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                }
                case TRANSACTION_onGetToken: {
                    data.enforceInterface(descriptor);
                    java.lang.String _result = this.onGetToken();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                }
                case TRANSACTION_onGetServerIP: {
                    data.enforceInterface(descriptor);
                    java.lang.String _result = this.onGetServerIP();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                }
                case TRANSACTION_onGetZigbeeNetInfo: {
                    data.enforceInterface(descriptor);
                    java.lang.String _result = this.onGetZigbeeNetInfo();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                }
                case TRANSACTION_onDisAlarmInfo: {
                    data.enforceInterface(descriptor);
                    int _arg0;
                    _arg0 = data.readInt();
                    int _arg1;
                    _arg1 = data.readInt();
                    this.onDisAlarmInfo(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_onSetSceneStatus: {
                    data.enforceInterface(descriptor);
                    long _arg0;
                    _arg0 = data.readLong();
                    this.onSetSceneStatus(_arg0);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_onGetDeviceList: {
                    data.enforceInterface(descriptor);
                    int _arg0;
                    _arg0 = data.readInt();
                    java.lang.String _result = this.onGetDeviceList(_arg0);
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                }
                case TRANSACTION_onSetSceneGWHidden: {
                    data.enforceInterface(descriptor);
                    long _arg0;
                    _arg0 = data.readLong();
                    int _arg1;
                    _arg1 = data.readInt();
                    boolean _result = this.onSetSceneGWHidden(_arg0, _arg1);
                    reply.writeNoException();
                    reply.writeInt(((_result) ? (1) : (0)));
                    return true;
                }
                case TRANSACTION_onGetRoomList: {
                    data.enforceInterface(descriptor);
                    java.lang.String _result = this.onGetRoomList();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                }
                case TRANSACTION_onGetSceneList: {
                    data.enforceInterface(descriptor);
                    java.lang.String _result = this.onGetSceneList();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                }
                case TRANSACTION_onCheckAlarmStatus: {
                    data.enforceInterface(descriptor);
                    this.onCheckAlarmStatus();
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_onJYJniReRegisterEnvInfo: {
                    data.enforceInterface(descriptor);
                    this.onJYJniReRegisterEnvInfo();
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_onSetDeviceStatus: {
                    data.enforceInterface(descriptor);
                    int _arg0;
                    _arg0 = data.readInt();
                    int _arg1;
                    _arg1 = data.readInt();
                    int _arg2;
                    _arg2 = data.readInt();
                    boolean _result = this.onSetDeviceStatus(_arg0, _arg1, _arg2);
                    reply.writeNoException();
                    reply.writeInt(((_result) ? (1) : (0)));
                    return true;
                }
                case TRANSACTION_onGetDeviceTypeInfo: {
                    data.enforceInterface(descriptor);
                    int _arg0;
                    _arg0 = data.readInt();
                    int _arg1;
                    _arg1 = data.readInt();
                    java.lang.String _result = this.onGetDeviceTypeInfo(_arg0, _arg1);
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                }
                default: {
                    return super.onTransact(code, data, reply, flags);
                }
            }
        }

        private static class Proxy implements com.hwellyi.smarthome.IMyAidlInterface {
            private android.os.IBinder mRemote;

            Proxy(android.os.IBinder remote) {
                mRemote = remote;
            }

            @Override
            public android.os.IBinder asBinder() {
                return mRemote;
            }

            public java.lang.String getInterfaceDescriptor() {
                return DESCRIPTOR;
            }

            @Override
            public void onPrintLogToJni(java.lang.String logstr) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(logstr);
                    boolean _status = mRemote.transact(Stub.TRANSACTION_onPrintLogToJni, _data, _reply, 0);
                    if (!_status && getDefaultImpl() != null) {
                        getDefaultImpl().onPrintLogToJni(logstr);
                        return;
                    }
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public boolean onGetNetWorkStatus() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    boolean _status = mRemote.transact(Stub.TRANSACTION_onGetNetWorkStatus, _data, _reply, 0);
                    if (!_status && getDefaultImpl() != null) {
                        return getDefaultImpl().onGetNetWorkStatus();
                    }
                    _reply.readException();
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public java.lang.String onGetSerial() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                java.lang.String _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    boolean _status = mRemote.transact(Stub.TRANSACTION_onGetSerial, _data, _reply, 0);
                    if (!_status && getDefaultImpl() != null) {
                        return getDefaultImpl().onGetSerial();
                    }
                    _reply.readException();
                    _result = _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public java.lang.String onGetToken() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                java.lang.String _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    boolean _status = mRemote.transact(Stub.TRANSACTION_onGetToken, _data, _reply, 0);
                    if (!_status && getDefaultImpl() != null) {
                        return getDefaultImpl().onGetToken();
                    }
                    _reply.readException();
                    _result = _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public java.lang.String onGetServerIP() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                java.lang.String _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    boolean _status = mRemote.transact(Stub.TRANSACTION_onGetServerIP, _data, _reply, 0);
                    if (!_status && getDefaultImpl() != null) {
                        return getDefaultImpl().onGetServerIP();
                    }
                    _reply.readException();
                    _result = _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public java.lang.String onGetZigbeeNetInfo() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                java.lang.String _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    boolean _status = mRemote.transact(Stub.TRANSACTION_onGetZigbeeNetInfo, _data, _reply, 0);
                    if (!_status && getDefaultImpl() != null) {
                        return getDefaultImpl().onGetZigbeeNetInfo();
                    }
                    _reply.readException();
                    _result = _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public void onDisAlarmInfo(int devid, int type) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(devid);
                    _data.writeInt(type);
                    boolean _status = mRemote.transact(Stub.TRANSACTION_onDisAlarmInfo, _data, _reply, 0);
                    if (!_status && getDefaultImpl() != null) {
                        getDefaultImpl().onDisAlarmInfo(devid, type);
                        return;
                    }
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void onSetSceneStatus(long sceneid) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeLong(sceneid);
                    boolean _status = mRemote.transact(Stub.TRANSACTION_onSetSceneStatus, _data, _reply, 0);
                    if (!_status && getDefaultImpl() != null) {
                        getDefaultImpl().onSetSceneStatus(sceneid);
                        return;
                    }
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public java.lang.String onGetDeviceList(int flag) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                java.lang.String _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(flag);
                    boolean _status = mRemote.transact(Stub.TRANSACTION_onGetDeviceList, _data, _reply, 0);
                    if (!_status && getDefaultImpl() != null) {
                        return getDefaultImpl().onGetDeviceList(flag);
                    }
                    _reply.readException();
                    _result = _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public boolean onSetSceneGWHidden(long sceneid, int value) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeLong(sceneid);
                    _data.writeInt(value);
                    boolean _status = mRemote.transact(Stub.TRANSACTION_onSetSceneGWHidden, _data, _reply, 0);
                    if (!_status && getDefaultImpl() != null) {
                        return getDefaultImpl().onSetSceneGWHidden(sceneid, value);
                    }
                    _reply.readException();
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public java.lang.String onGetRoomList() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                java.lang.String _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    boolean _status = mRemote.transact(Stub.TRANSACTION_onGetRoomList, _data, _reply, 0);
                    if (!_status && getDefaultImpl() != null) {
                        return getDefaultImpl().onGetRoomList();
                    }
                    _reply.readException();
                    _result = _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public java.lang.String onGetSceneList() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                java.lang.String _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    boolean _status = mRemote.transact(Stub.TRANSACTION_onGetSceneList, _data, _reply, 0);
                    if (!_status && getDefaultImpl() != null) {
                        return getDefaultImpl().onGetSceneList();
                    }
                    _reply.readException();
                    _result = _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public void onCheckAlarmStatus() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    boolean _status = mRemote.transact(Stub.TRANSACTION_onCheckAlarmStatus, _data, _reply, 0);
                    if (!_status && getDefaultImpl() != null) {
                        getDefaultImpl().onCheckAlarmStatus();
                        return;
                    }
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void onJYJniReRegisterEnvInfo() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    boolean _status = mRemote.transact(Stub.TRANSACTION_onJYJniReRegisterEnvInfo, _data, _reply, 0);
                    if (!_status && getDefaultImpl() != null) {
                        getDefaultImpl().onJYJniReRegisterEnvInfo();
                        return;
                    }
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public boolean onSetDeviceStatus(int devid, int subid, int status) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(devid);
                    _data.writeInt(subid);
                    _data.writeInt(status);
                    boolean _status = mRemote.transact(Stub.TRANSACTION_onSetDeviceStatus, _data, _reply, 0);
                    if (!_status && getDefaultImpl() != null) {
                        return getDefaultImpl().onSetDeviceStatus(devid, subid, status);
                    }
                    _reply.readException();
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public java.lang.String onGetDeviceTypeInfo(int devid, int type) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                java.lang.String _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(devid);
                    _data.writeInt(type);
                    boolean _status = mRemote.transact(Stub.TRANSACTION_onGetDeviceTypeInfo, _data, _reply, 0);
                    if (!_status && getDefaultImpl() != null) {
                        return getDefaultImpl().onGetDeviceTypeInfo(devid, type);
                    }
                    _reply.readException();
                    _result = _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            public static com.hwellyi.smarthome.IMyAidlInterface sDefaultImpl;
        }

        static final int TRANSACTION_onPrintLogToJni = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_onGetNetWorkStatus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
        static final int TRANSACTION_onGetSerial = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
        static final int TRANSACTION_onGetToken = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
        static final int TRANSACTION_onGetServerIP = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
        static final int TRANSACTION_onGetZigbeeNetInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
        static final int TRANSACTION_onDisAlarmInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
        static final int TRANSACTION_onSetSceneStatus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
        static final int TRANSACTION_onGetDeviceList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
        static final int TRANSACTION_onSetSceneGWHidden = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
        static final int TRANSACTION_onGetRoomList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
        static final int TRANSACTION_onGetSceneList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
        static final int TRANSACTION_onCheckAlarmStatus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
        static final int TRANSACTION_onJYJniReRegisterEnvInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
        static final int TRANSACTION_onSetDeviceStatus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
        static final int TRANSACTION_onGetDeviceTypeInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);

        public static boolean setDefaultImpl(com.hwellyi.smarthome.IMyAidlInterface impl) {
            if (Stub.Proxy.sDefaultImpl == null) {
                Stub.Proxy.sDefaultImpl = impl;
                return true;
            }
            return false;
        }

        public static com.hwellyi.smarthome.IMyAidlInterface getDefaultImpl() {
            return Stub.Proxy.sDefaultImpl;
        }
    }

    public void onPrintLogToJni(java.lang.String logstr) throws android.os.RemoteException;

    public boolean onGetNetWorkStatus() throws android.os.RemoteException;

    public java.lang.String onGetSerial() throws android.os.RemoteException;

    public java.lang.String onGetToken() throws android.os.RemoteException;

    public java.lang.String onGetServerIP() throws android.os.RemoteException;

    public java.lang.String onGetZigbeeNetInfo() throws android.os.RemoteException;

    public void onDisAlarmInfo(int devid, int type) throws android.os.RemoteException;

    public void onSetSceneStatus(long sceneid) throws android.os.RemoteException;

    public java.lang.String onGetDeviceList(int flag) throws android.os.RemoteException;

    public boolean onSetSceneGWHidden(long sceneid, int value) throws android.os.RemoteException;

    public java.lang.String onGetRoomList() throws android.os.RemoteException;

    public java.lang.String onGetSceneList() throws android.os.RemoteException;

    public void onCheckAlarmStatus() throws android.os.RemoteException;

    public void onJYJniReRegisterEnvInfo() throws android.os.RemoteException;

    public boolean onSetDeviceStatus(int devid, int subid, int status) throws android.os.RemoteException;

    public java.lang.String onGetDeviceTypeInfo(int devid, int type) throws android.os.RemoteException;
}