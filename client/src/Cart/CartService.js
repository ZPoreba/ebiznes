import moment from 'moment';
const API_URL = process.env.REACT_APP_API_URL;


const checkStatus = (response) => {
    if(!response.ok) {
        throw Error(response.status);
    }
    return response;
}

const getBucketProducts = () => {
    let url = `${API_URL}/cart`;
    let user = JSON.parse(localStorage.getItem('user'));

    const cartQs = {
        id: user.user_id
    };

    const fetchData = {
        method: 'GET',
        mode: "cors",
        headers: new Headers(),
        redirect: 'follow'
    };

    let esc = encodeURIComponent;
    let query = Object.keys(cartQs)
        .map(k => esc(k) + '=' + esc(cartQs[k]))
        .join('&');

    url = url + "?" + query;

    return fetch(url, fetchData)
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

    const fetchData = {
        method: 'GET',
        mode: "cors",
        headers: new Headers(),
        redirect: 'follow'
    };

    let esc = encodeURIComponent;
    let query = Object.keys(productQs)
        .map(k => esc(k) + '=' + esc(productQs[k]))
        .join('&');

    url = url + "?" + query;

    return fetch(url, fetchData)
        .then(response => checkStatus(response))
        .then(response => response.text())
        .then(response => JSON.parse(response))
        .catch(error => {
            return {success: false, status: error.message}
        });
}

const deleteProductForUser = (productId) => {
    let url = `${API_URL}/cart/deleteProductForUser`;
    let user = JSON.parse(localStorage.getItem('user'));

    const productQs = {
        productId: productId,
        userId: user.user_id
    };

    const fetchData = {
        method: 'DELETE',
        mode: "cors",
        headers: new Headers(),
        redirect: 'follow'
    };

    let esc = encodeURIComponent;
    let query = Object.keys(productQs)
        .map(k => esc(k) + '=' + esc(productQs[k]))
        .join('&');

    url = url + "?" + query;

    return fetch(url, fetchData)
        .then(response => checkStatus(response))
        .then(response => response.text())
        .catch(error => {
            return {success: false, status: error.message}
        });
}

const createPayment = () => {
    let url = `${API_URL}/payment`;
    let date = moment().format('YYYY-MM-DD');

    const productQs = {
        date: date,
        status: 'done'
    };

    const fetchData = {
        method: 'POST',
        mode: "cors",
        headers: new Headers(),
        redirect: 'follow'
    };

    let esc = encodeURIComponent;
    let query = Object.keys(productQs)
        .map(k => esc(k) + '=' + esc(productQs[k]))
        .join('&');

    url = url + "?" + query;

    return fetch(url, fetchData)
        .then(response => checkStatus(response))
        .then(response => response.text())
        .then(response => JSON.parse(response))
        .catch(error => {
            return {success: false, status: error.message}
        });
}

const createOrder = (orderData) => {
    let url = `${API_URL}/order`;
    const orderQs = orderData;

    const fetchData = {
        method: 'POST',
        mode: "cors",
        headers: new Headers(),
        redirect: 'follow'
    };

    let esc = encodeURIComponent;
    let query = Object.keys(orderQs)
        .map(k => esc(k) + '=' + esc(orderQs[k]))
        .join('&');

    url = url + "?" + query;

    return fetch(url, fetchData)
        .then(response => checkStatus(response))
        .then(response => response.text())
        .catch(error => {
            return {success: false, status: error.message}
        });
}

const deleteCart = () => {
    let url = `${API_URL}/cart`;
    let user = JSON.parse(localStorage.getItem('user'));

    const cartQs = {
        userId: user.user_id
    };

    const fetchData = {
        method: 'DELETE',
        mode: "cors",
        headers: new Headers(),
        redirect: 'follow'
    };

    let esc = encodeURIComponent;
    let query = Object.keys(cartQs)
        .map(k => esc(k) + '=' + esc(cartQs[k]))
        .join('&');

    url = url + "?" + query;

    return fetch(url, fetchData)
        .then(response => checkStatus(response))
        .then(response => response.text())
        .catch(error => {
            return {success: false, status: error.message}
        });
}

const checkDiscountCode = (code, products) => {
    let url = `${API_URL}/discountcode/check`;

    const codeQs = {
        code: code,
        products: products
    };

    const fetchData = {
        method: 'GET',
        mode: "cors",
        headers: new Headers(),
        redirect: 'follow'
    };

    let esc = encodeURIComponent;
    let query = Object.keys(codeQs)
        .map(k => esc(k) + '=' + esc(codeQs[k]))
        .join('&');

    url = url + "?" + query;

    return fetch(url, fetchData)
        .then(response => checkStatus(response))
        .then(response => response.text())
        .then(response => JSON.parse(response))
        .catch(error => {
            return {success: false, status: error.message}
        });
}

export const cartService = { getBucketProducts, getProductById, deleteProductForUser, createPayment, createOrder,
                            deleteCart, checkDiscountCode };