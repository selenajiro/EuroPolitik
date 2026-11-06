// Helpers shared by more than one page. Load it after /js/auth.js.

// Linear blend between two #rrggbb colors: t = 0 gives a, t = 1 gives b.
function blendHex(a, b, t) {
    const av = parseInt(a.slice(1), 16), bv = parseInt(b.slice(1), 16);
    const ar = (av >> 16) & 255, ag = (av >> 8) & 255, ab = av & 255;
    const br = (bv >> 16) & 255, bg = (bv >> 8) & 255, bb = bv & 255;
    const r = Math.round(ar + (br - ar) * t);
    const g = Math.round(ag + (bg - ag) * t);
    const bl = Math.round(ab + (bb - ab) * t);
    return `#${[r, g, bl].map(x => x.toString(16).padStart(2, '0')).join('')}`;
}
