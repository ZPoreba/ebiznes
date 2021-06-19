const API_URL = process.env.REACT_APP_API_URL;

const checkStatus = (response) => {
    if(!response.ok) {
        throw Error(response.status);
    }
    return response;
}

const login = async (email, password, rememberMe) => {

    let formdata = new FormData();
    formdata.append("email", email);
    formdata.append("password", password);
    formdata.append("rememberMe", rememberMe);

    const fetchData = {
        method: 'POST',
        mode: "cors",
        body: formdata,
        redirect: 'follow',
        credentials: 'include'
    };

    return fetch(`${API_URL}/authenticate/credentials`, fetchData)
        .then(response => checkStatus(response))
        .then(response => response.text())
        .then(response => JSON.parse(response))
        .catch(error => {
            return {success: false, status: error.message}
        });

}

const setUser = (data) => {
    let user = {
        user_id: data.id
    }
    window.localStorage.setItem('user', JSON.stringify(user));
}

const loginWithGoogle = () => {
    window.location.href = `${API_URL}/authenticate/google`;
}

const loginWithFacebook = () => {
    window.location.href = `${API_URL}/authenticate/facebook`;
}

export const loginService = { login, loginWithGoogle, setUser, loginWithFacebook };