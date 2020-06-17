import React, {Component} from 'react';
import {withRouter} from "react-router-dom";
import {loginService} from "./LoginService";


const API_URL = process.env.REACT_APP_API_URL;

class AuthProvider extends Component {

    constructor(props) {
        super(props);
    }

    componentDidMount() {
        const fetchData = {
            method: 'GET',
            redirect: 'follow',
            credentials: 'include'
        };

        if(this.props.location.search.length > 0) {
            fetch(`${API_URL}/authenticate/${this.props.provider}${this.props.location.search}`, fetchData)
                .then(response => response.json())
                .then(resp => {
                    loginService.setUser(resp);
                    this.props.history.push('/search');
                });
        }
        else {
            this.props.history.push('/login');
        }
    }

    render() {
        return (<div />);
    }
};

export default withRouter(AuthProvider);