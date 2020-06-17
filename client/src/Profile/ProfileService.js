const API_URL = process.env.REACT_APP_API_URL;

const checkStatus = (response) => {
    if(!response.ok) {
        throw Error(response.status);
    }
    return response;
}

const getUserData = () => {
    let url = `${API_URL}/user`;
    let user = JSON.parse(localStorage.getItem('user'));

    const userQs = {
        id: user.user_id
    };

    const fetchData = {
        method: 'GET',
        mode: "cors",
        headers: new Headers(),
        redirect: 'follow'
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

const putUserData = (data) => {
    let url = `${API_URL}/user`;
    let user = JSON.parse(localStorage.getItem('user'));

    const userQs = {
        id: user.user_id,
        firstName: data.firstName,
        lastName: data.lastName,
        address: data.address
    };

    const fetchData = {
        method: 'PUT',
        headers: new Headers(),
        redirect: 'follow'
    };

    let esc = encodeURIComponent;
    let query = Object.keys(userQs)
        .map(k => esc(k) + '=' + esc(userQs[k]))
        .join('&');

    url = url + "?" + query;

    return fetch(url, fetchData)
        .then(response => checkStatus(response))
        .then(response => response.text())
        .catch(error => {
            return {success: false, status: error.message}
        });
}

export const profileService = { getUserData, putUserData };