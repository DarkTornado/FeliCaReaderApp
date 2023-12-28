package com.darktornado.library;

import android.util.SparseArray;

import java.io.ByteArrayOutputStream;

public class FeliCa {

    public static final int TYPE_JR = 0;
    public static final int TYPE_METRO = 1;
    public static final int TYPE_BUS = 2;
    public static final int TYPE_GOODS = 3;


    public int deviceId, procId, year, month, date, type, balance, index, regionId;
    public int inStn, inLn;
    public int outStn, outLn;

    public String device;
    public String action;

    public static SparseArray<String> deviceList, actionList;

    protected FeliCa() {
        // https://osdn.net/projects/felicalib/wiki/suica
        if (deviceList == null) {
            deviceList = new SparseArray<>();
            deviceList.put(3, "정산기");
            deviceList.put(4, "휴대용 단말기");
            deviceList.put(5, "자동차 단말기");
            deviceList.put(7, "발매기");
            deviceList.put(8, "발매기");
            deviceList.put(9, "입금기");
            deviceList.put(18, "발매기");
            deviceList.put(20, "발매기 등");
            deviceList.put(21, "발매기 등");
            deviceList.put(22, "개찰기");
            deviceList.put(23, "간이 개찰기");
            deviceList.put(24, "창구 단말기");
            deviceList.put(25, "창구 단말기");
            deviceList.put(26, "개찰 단말기");
            deviceList.put(27, "휴대폰");
            deviceList.put(28, "승계 정산기");
            deviceList.put(29, "연락 개찰기");
            deviceList.put(31, "간이 입금기");
            deviceList.put(70, "VIEW ALTTE");
            deviceList.put(72, "VIEW ALTTE");
            deviceList.put(199, "물판 단말기");
            deviceList.put(200, "자판기");
        }
        if (actionList == null) {
            actionList = new SparseArray<>();
            actionList.put(1, "운임 지불 (개찰구 퇴장)");
            actionList.put(2, "충전");
            actionList.put(3, "표 구매");
            actionList.put(4, "정산");
            actionList.put(5, "입장 정산");
            actionList.put(6, "퇴장 (개찰 창구 처리)");
            actionList.put(7, "신규");
            actionList.put(8, "창구공제");
            actionList.put(13, "버스 (PiTaPa)");
            actionList.put(15, "버스 (IruCa)");
            actionList.put(17, "재발행 처리");
            actionList.put(19, "신칸센 이용");
            actionList.put(20, "자동 충전 (개찰구 입장)");
            actionList.put(21, "자동 충전 (개찰구 퇴장)");
            actionList.put(31, "입금 (버스 충전)");
            actionList.put(35, "버스 노면전차 기획권 구매");
            actionList.put(70, "물건 구매");
            actionList.put(72, "특전 (특전 요금)");
            actionList.put(73, "입금 (계산대 입금)");
            actionList.put(74, "물건 구매 취소");
            actionList.put(75, "입장 물판");
            actionList.put(198, "현금 병용 물판");
            actionList.put(203, "입장 현금 병용 물판");
            actionList.put(132, "타사 정산");
            actionList.put(133, "타사 입장 정산");
        }
    }

    public static FeliCa parse(byte[] res, int offset) {
        FeliCa fc = new FeliCa();
        fc.init(res, offset);
        return fc;
    }

    protected void init(byte[] res, int offset) {
        this.deviceId = res[offset + 0];
        this.procId = res[offset + 1];

        int mixInt = toInt(res, offset, 4, 5);
        this.year = ((mixInt >> 9) & 0x07f) + 2000;
        this.month = (mixInt >> 5) & 0x00f;
        this.date = mixInt & 0x01f;

        if (isShopping(procId)) {
            this.type = TYPE_GOODS;
        } else if (isBus(procId)) {
            this.type = TYPE_BUS;
        } else {
            this.type = res[offset + 6] < 0x80 ? TYPE_JR : TYPE_METRO;
        }

        this.inLn = toInt(res, offset, 6);
        this.inStn = toInt(res, offset, 7);
        this.outLn = toInt(res, offset, 8);
        this.outStn = toInt(res, offset, 9);

        this.balance = toInt(res, offset, 11, 10);
        this.index = toInt(res, offset, 12, 13, 14);
        this.regionId = res[offset + 15];

        this.device = deviceList.get(this.deviceId);
        this.action = actionList.get(this.procId);
    }

    private int toInt(byte[] res, int offset, int... idx) {
        int num = 0;
        for (int i = 0; i < idx.length; i++) {
            num = num << 8;
            num += ((int) res[offset + idx[i]]) & 0x0ff;
        }
        return num;
    }

    private boolean isShopping(int procId) {
        return procId == 70 || procId == 73 || procId == 74 || procId == 75 || procId == 198 || procId == 203;
    }

    private boolean isBus(int procId) {
        return procId == 13 || procId == 15 || procId == 31 || procId == 35;
    }

    public static byte[] readWithoutEncryption(byte[] idm, int size) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(100);

        bout.write(0);
        bout.write(0x06);
        bout.write(idm);
        bout.write(1);
        bout.write(0x0f);
        bout.write(0x09);
        bout.write(size);
        for (int n = 0; n < size; n++) {
            bout.write(0x80);
            bout.write(n);
        }

        byte[] msg = bout.toByteArray();
        msg[0] = (byte) msg.length;
        return msg;
    }

}