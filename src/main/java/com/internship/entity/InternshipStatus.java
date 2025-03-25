package com.internship.entity;

public enum InternshipStatus {
    PENDING,                // İlk başvuru durumu
    ADVISOR_APPROVED,       // Danışman onayladı
    ADVISOR_REJECTED,       // Danışman reddetti
    REVISION_REQUESTED,     // Düzeltme istendi
    DEPARTMENT_APPROVED,    // Bölüm koordinatörü onayladı
    DEPARTMENT_REJECTED,    // Bölüm koordinatörü reddetti
    UNIVERSITY_APPROVED,    // Üniversite koordinatörü onayladı
    UNIVERSITY_REJECTED,    // Üniversite koordinatörü reddetti
    CANCELLED,             // İptal edildi
    EXTENDED,              // Uzatıldı
    COMPLETED,             // Tamamlandı
    EVALUATED              // Değerlendirildi
} 