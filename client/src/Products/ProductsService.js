const API_URL = process.env.REACT_APP_API_URL;

const checkStatus = (response) => {
    if(!response.ok) {
        throw Error(response.status);
    }
    return response;
}

const getProducts = (categoryId) => {
    let url = `${API_URL}/productFromCategory`;

    const productsQs = {
        categoryId: categoryId
    };

    const fetchData = {
        method: 'GET',
        mode: "cors",
        redirect: 'follow',
        credentials: 'include'
    };

    let esc = encodeURIComponent;
    let query = Object.keys(productsQs)
        .map(k => esc(k) + '=' + esc(productsQs[k]))
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

const getAllProducts = () => {
    let url = `${API_URL}/products`;

    const fetchData = {
        method: 'GET',
        mode: "cors",
        redirect: 'follow',
        credentials: 'include'
    };

    return fetch(url, fetchData)
        .then(response => checkStatus(response))
        .then(response => response.text())
        .then(response => JSON.parse(response))
        .catch(error => {
            return {success: false, status: error.message}
        });
}

const getOpinionsForProduct = (productId) => {
    let url = `${API_URL}/opinionsForProduct`;

    const productsQs = {
        productId: productId
    };

    const fetchData = {
        method: 'GET',
        mode: "cors",
        redirect: 'follow',
        credentials: 'include'
    };

    let esc = encodeURIComponent;
    let query = Object.keys(productsQs)
        .map(k => esc(k) + '=' + esc(productsQs[k]))
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

const addToBucket = (productId) => {
    let url = `${API_URL}/cart`;
    let user = JSON.parse(localStorage.getItem('user'));

    const productsQs = {
        userId: user.user_id,
        products: productId
    };

    const fetchData = {
        method: 'POST',
        mode: "cors",
        redirect: 'follow',
        credentials: 'include'
    };

    let esc = encodeURIComponent;
    let query = Object.keys(productsQs)
        .map(k => esc(k) + '=' + esc(productsQs[k]))
        .join('&');

    url = url + "?" + query;

    return fetch(url, fetchData)
        .then(response => checkStatus(response))
        .then(response => response.text())
        .catch(error => {
            return {success: false, status: error.message}
        });
}

const addToWishList = (productId) => {
    let url = `${API_URL}/wishlist`;
    let user = JSON.parse(localStorage.getItem('user'));

    const productsQs = {
        userId: user.user_id,
        products: productId
    };

    const fetchData = {
        method: 'POST',
        mode: "cors",
        redirect: 'follow',
        credentials: 'include'
    };

    let esc = encodeURIComponent;
    let query = Object.keys(productsQs)
        .map(k => esc(k) + '=' + esc(productsQs[k]))
        .join('&');

    url = url + "?" + query;

    return fetch(url, fetchData)
        .then(response => checkStatus(response))
        .then(response => response.text())
        .catch(error => {
            return {success: false, status: error.message}
        });
}

const getUserById = (id) => {
    let url = `${API_URL}/user`;

    const userQs = {
        id: id
    };

    const fetchData = {
        method: 'GET',
        mode: "cors",
        redirect: 'follow',
        credentials: 'include'
    };

    let esc = encodeURIComponent;
    let query = Object.keys(userQs)
        .map(k => esc(k) + '=' + esc(userQs[k]))
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

export const productsService = { getProducts, getAllProducts, getOpinionsForProduct, addToBucket, addToWishList,
                                getUserById };