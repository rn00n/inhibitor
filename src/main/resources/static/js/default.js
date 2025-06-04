const CLIENT_ID = 'backoffice';
const CLIENT_SECRET = 'GZhM5AYK#qCv9RxwUpbNeXtDJF_L2amH';
const TOKEN_ENDPOINT = '/oauth2/token';
const LOGIN_PAGE = '/backoffice/login';
let refreshingTokenPromise = null;
/** access_token 없으면 튕김 */
function ensureAccessTokenOrRedirect() {
    const token = localStorage.getItem('access_token');
    if (!token) {
        const currentUrl = encodeURIComponent(window.location.href);
        window.location.href = `${LOGIN_PAGE}?redirect=${currentUrl}`;
        return false;
    }
    return true;
}

/** 기본 fetch 호출 → 실패 시 refresh → 성공 시 재요청 */
async function doFetch(method, {url, query, body, headers = {}}, retry = true) {
    const token = localStorage.getItem('access_token');
    if (!token) return redirectToLogin();

    const fullUrl = buildUrlWithQuery(url, query);
    const fetchOptions = {
        method,
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
            ...headers,
        },
    };

    if (body && ['POST', 'PUT', 'PATCH'].includes(method.toUpperCase())) {
        fetchOptions.body = JSON.stringify(body);
    }

    const res = await fetch(fullUrl, fetchOptions);

    if (res.status === 401 && retry) {
        if (!refreshingTokenPromise) {
            refreshingTokenPromise = tryRefreshToken();
        }

        const refreshed = await refreshingTokenPromise;
        refreshingTokenPromise = null;

        if (refreshed) {
            return doFetch(method, {url, query, body, headers}, false);
        } else {
            return redirectToLogin();
        }
    }

    return res;
}

/** URL + query param 붙이기 */
function buildUrlWithQuery(url, query) {
    if (!query) return url;
    const search = new URLSearchParams(query).toString();
    return url + (url.includes('?') ? '&' : '?') + search;
}

/** 토큰 갱신 요청 */
async function tryRefreshToken() {
    const refreshToken = localStorage.getItem('refresh_token');
    if (!refreshToken) return false;

    const params = new URLSearchParams();
    params.append('grant_type', 'refresh_token');
    params.append('refresh_token', refreshToken);

    const basicAuth = btoa(`${CLIENT_ID}:${CLIENT_SECRET}`);

    const res = await fetch(TOKEN_ENDPOINT, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'Authorization': `Basic ${basicAuth}`
        },
        body: params.toString()
    });

    if (res.ok) {
        const data = await res.json();
        localStorage.setItem('access_token', data.access_token);
        localStorage.setItem('refresh_token', data.refresh_token);
        return true;
    } else {
        return false;
    }
}

/** 로그인 페이지로 튕기기 */
function redirectToLogin() {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    const currentUrl = encodeURIComponent(window.location.href);
    window.location.href = `${LOGIN_PAGE}?redirect=${currentUrl}`;
}

async function getAccountInfo() {
    let response = await doFetch('GET', {
        url: '/backoffice/api/profiles/me',
        query: {page: 1}
    });

    const data = await response.json();
    showToast(JSON.stringify(data));
}

(async () => {
    ensureAccessTokenOrRedirect();
})();
