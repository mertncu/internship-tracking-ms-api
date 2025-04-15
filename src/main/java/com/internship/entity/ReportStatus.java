package com.internship.entity;

public enum ReportStatus {
    PENDING,       // Rapor yüklendi, henüz değerlendirilmedi
    APPROVED,      // Rapor danışman tarafından onaylandı
    REJECTED,      // Rapor danışman tarafından reddedildi
    REVISION,      // Rapor için revizyon istendi
    COMPLETED      // Revizyon yapıldı ve onaylandı
} 