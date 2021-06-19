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

const getOrdersForUser = () => {
    let url = `${API_URL}/ordersForUser`;
    let user = JSON.parse(localStorage.getItem('user'));

    const ordersQs = {
        userId: user.user_id
    };

    let esc = encodeURIComponent;
    let query = Object.keys(ordersQs)
        .map(k => esc(k) + '=' + esc(ordersQs[k]))
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

const getOrderById = (orderId) => {
    let url = `${API_URL}/order`;

    const orderQs = {
        id: orderId
    };

    let esc = encodeURIComponent;
    let query = Object.keys(orderQs)
        .map(k => esc(k) + '=' + esc(orderQs[k]))
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

const createReturn = (productId) => {
    let url = `${API_URL}/return`;
    let user = JSON.parse(localStorage.getItem('user'));

    const returnQs = {
        userId: user.user_id,
        productId: productId,
        status: 'pending'
    };

    let esc = encodeURIComponent;
    let query = Object.keys(returnQs)
        .map(k => esc(k) + '=' + esc(returnQs[k]))
        .join('&');

    url = url + "?" + query;

    return fetch(url, fetchData('POST'))
        .then(response => checkStatus(response))
        .then(response => response.text())
        .catch(error => {
            return {success: false, status: error.message}
        });
}

const createOpinion = (content, productId) => {
    let url = `${API_URL}/opinion`;
    let user = JSON.parse(localStorage.getItem('user'));

    const opinionQs = {
        userId: user.user_id,
        productId: productId,
        content: content
    };

    let esc = encodeURIComponent;
    let query = Object.keys(opinionQs)
        .map(k => esc(k) + '=' + esc(opinionQs[k]))
        .join('&');

    url = url + "?" + query;

    return fetch(url, fetchData('POST'))
        .then(response => checkStatus(response))
        .then(response => response.text())
        .catch(error => {
            return {success: false, status: error.message}
        });
}

export const ordersService = { getOrdersForUser, getOrderById, getProductById, createReturn, createOpinion };