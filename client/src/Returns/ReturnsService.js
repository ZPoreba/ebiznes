const API_URL = process.env.REACT_APP_API_URL;

const checkStatus = (response) => {
    if(!response.ok) {
        throw Error(response.status);
    }
    return response;
}

const fetchData = (method) => {
    return {
        method: method,
        mode: "cors",
        redirect: 'follow',
        credentials: 'include'
    }
}

const getReturnsProducts = () => {
    let url = `${API_URL}/returnsForUser`;
    let user = JSON.parse(localStorage.getItem('user'));

    const returnsQs = {
        userId: user.user_id
    };

    let esc = encodeURIComponent;
    let query = Object.keys(returnsQs)
        .map(k => esc(k) + '=' + esc(returnsQs[k]))
        .join('&');

    url = url + "?" + query;

    return fetch(url, fetchData('GET'))
        .then(response => checkStatus(response))
        .then(response => response.text())
        .then(response => JSON.parse(response))
        .catch(error => {
            return {success: false, status: error.message}
        });
}

const getProductById = (productId) => {
    let url = `${API_URL}/product`;

    const productQs = {
        id: productId
    };

    let esc = encodeURIComponent;
    let query = Object.keys(productQs)
        .map(k => esc(k) + '=' + esc(productQs[k]))
        .join('&');

    url = url + "?" + query;

    return fetch(url, fetchData('GET'))
        .then(response => checkStatus(response))
        .then(response => response.text())
        .then(response => JSON.parse(response))
        .catch(error => {
            return {success: false, status: error.message}
        });
}

const deleteReturn = (returnId) => {
    let url = `${API_URL}/return`;

    const returnQs = {
        id: returnId,
    };

    let esc = encodeURIComponent;
    let query = Object.keys(returnQs)
        .map(k => esc(k) + '=' + esc(returnQs[k]))
        .join('&');

    url = url + "?" + query;

    return fetch(url, fetchData('DELETE'))
        .then(response => checkStatus(response))
        .then(response => response.text())
        .catch(error => {
            return {success: false, status: error.message}
        });
}

export const returnsService = { getReturnsProducts, getProductById, deleteReturn };