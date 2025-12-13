/* ========== HOME PAGE SCRIPTS ========== */

// Game data
const games = [
    {
        name: 'Tiến Lên',
        logo: '/images/logo/tienlen.png',
        status: 'active',
        route: '/templates/tienlen.html'
    },
    {
        name: 'Ba Lá',
        logo: '/images/logo/bala.png',
        status: 'coming-soon'
    },
    {
        name: 'Bài Binh',
        logo: '/images/logo/baibinh.png',
        status: 'coming-soon'
    },
    {
        name: 'Xì Dách',
        logo: '/images/logo/xidach.png',
        status: 'coming-soon'
    },
    {
        name: 'Bầu Cua',
        logo: '/images/logo/baucua.png',
        status: 'coming-soon'
    },
    {
        name: 'Lô Tô',
        logo: '/images/logo/loto.png',
        status: 'coming-soon'
    }
];

let currentGameIndex = 0;

/**
 * Update game display
 */
function updateGameDisplay() {
    const game = games[currentGameIndex];
    const gameLogo = document.getElementById('gameLogo');
    const gameName = document.getElementById('gameName');

    // Add transition animation
    gameLogo.classList.add('transition-enter');

    // Update image and name with slight delay for animation
    setTimeout(() => {
        gameLogo.src = game.logo;
        gameName.textContent = game.name;
        gameLogo.classList.remove('transition-enter');
    }, 150);
}

/**
 * Go to previous game
 */
function previousGame() {
    currentGameIndex = (currentGameIndex - 1 + games.length) % games.length;
    updateGameDisplay();
}

/**
 * Go to next game
 */
function nextGame() {
    currentGameIndex = (currentGameIndex + 1) % games.length;
    updateGameDisplay();
}

/**
 * Play selected game
 */
function playGame() {
    const game = games[currentGameIndex];

    if (game.status === 'coming-soon') {
        showComingSoon();
        return;
    }

    if (game.status === 'active') {
        navigateTo(game.route);
    }
}

/**
 * Show coming soon message
 */
function showComingSoon() {
    const container = document.getElementById('comingSoonContainer');
    if (container) {
        container.classList.remove('hidden');
        setTimeout(() => {
            container.classList.add('hidden');
        }, 5000);
    }
}

/**
 * Close coming soon message
 */
function closeComingSoon() {
    const container = document.getElementById('comingSoonContainer');
    if (container) {
        container.classList.add('hidden');
    }
}

/**
 * Handle keyboard navigation
 */
document.addEventListener('keydown', (e) => {
    if (e.key === 'ArrowLeft') previousGame();
    if (e.key === 'ArrowRight') nextGame();
    if (e.key === 'Enter') playGame();
});

/**
 * Handle touch/swipe navigation for mobile
 */
let touchStartX = 0;
let touchEndX = 0;

document.addEventListener('touchstart', (e) => {
    touchStartX = e.changedTouches[0].screenX;
}, false);

document.addEventListener('touchend', (e) => {
    touchEndX = e.changedTouches[0].screenX;
    handleSwipe();
}, false);

function handleSwipe() {
    const swipeThreshold = 50;
    const diff = touchStartX - touchEndX;

    if (Math.abs(diff) > swipeThreshold) {
        if (diff > 0) {
            // Swiped left, next game
            nextGame();
        } else {
            // Swiped right, previous game
            previousGame();
        }
    }
}

// Initialize page
document.addEventListener('DOMContentLoaded', () => {
    // Verify authentication
    if (!verifyAuth()) {
        return;
    }

    // Load user data to header from localStorage
    // Dữ liệu này bao gồm:
    // - fullName: Từ thông tin đăng ký
    // - balance: Số dư thực tế (mặc định 1 triệu VNĐ cho tài khoản mới)
    loadUserDataToHeader();

    // Initialize game display
    updateGameDisplay();
});
