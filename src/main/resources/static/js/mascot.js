document.addEventListener("DOMContentLoaded", function() {
    // Login ve Register sayfalarındaki inputları yakala
    const emailInput = document.querySelector('input[name="email"]');
    const textInput = document.querySelector('input[name="fullname"]'); // Register için
    const passwordInput = document.querySelector('input[name="password"]');
    const mascot = document.getElementById('mascot');

    // --- PANDA RESİMLERİ (Direkt Linkler) ---
    const images = {
        idle: 'https://i.imgur.com/Z9X6d0o.png',      // Normal Duruş
        watching: 'https://i.imgur.com/Tf0f52Z.png',  // E-postaya Bakış
        hide: 'https://i.imgur.com/8g3Zf0T.png',      // Göz Kapatma
        peek: 'https://i.imgur.com/a0S7g1X.png'       // Aradan Bakma (Peek)
    };

    // --- HAREKET FONKSİYONLARI ---

    // 1. Normal Duruş
    function setIdle() {
        if(!mascot) return;
        mascot.src = images.idle;
        mascot.className = 'mascot-img';
    }

    // 2. Takip Etme (Mail yazarken)
    function setWatching() {
        if(!mascot) return;
        mascot.src = images.watching;
        mascot.classList.add('mascot-look');
    }

    // 3. Şifre Tepkisi (Gizlen veya Bak)
    function setPasswordReaction() {
        if(!mascot) return;
        // %50 ihtimalle ya tamamen kapatır ya da aradan bakar
        const reactions = [images.hide, images.peek];
        const randomReaction = reactions[Math.floor(Math.random() * reactions.length)];
        
        mascot.src = randomReaction;
        mascot.classList.add('mascot-hide');
    }

    // --- OLAY DİNLEYİCİLERİ ---

    // E-posta alanı
    if (emailInput) {
        emailInput.addEventListener('focus', setWatching);
        emailInput.addEventListener('blur', setIdle);
    }
    
    // İsim alanı (Kayıt sayfası için)
    if (textInput) {
        textInput.addEventListener('focus', setWatching);
        textInput.addEventListener('blur', setIdle);
    }

    // Şifre alanı
    if (passwordInput) {
        passwordInput.addEventListener('focus', setPasswordReaction);
        passwordInput.addEventListener('blur', setIdle);
    }

    // Sayfa açılınca normal duruş
    setIdle();
});