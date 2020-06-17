import React, { Component } from 'react';
import './style.css';
import "antd/dist/antd.css";
import { Card, Input, Button, Avatar } from 'antd';
import { profileService } from './ProfileService';
import {withRouter} from "react-router-dom";
import {commonService} from "../Common/CommonService";

const { Meta } = Card;

class ProfileView extends Component {

    constructor(props) {
        super(props);
        this.state = {
            originalData: undefined,
            userData: undefined,
            readOnly: true
        };
    }

    componentDidMount() {
        commonService.checkIfAuthenticated(this.props).then(result => {
            if(result) this.loadUserData();
        });
    }

    loadUserData = () => {
        profileService.getUserData().then( data => {
            let userData = Object.assign({}, data);
            let originalData = Object.assign({}, data);
            this.setState({userData: userData, originalData: originalData});
        });
    }

    startEditing = () => {
        this.setState({readOnly: false});
    }

    cancelEditing = () => {
        this.refs.firstName.setState({value: this.state.originalData.firstName});
        this.refs.lastName.setState({value: this.state.originalData.lastName});
        this.refs.address.setState({value: this.state.originalData.address});
        this.setState({readOnly: true});
    }

    saveEditing = () => {
        let firstName = this.refs.firstName.state.value;
        let lastName = this.refs.lastName.state.value;
        let address = this.refs.address.state.value;

        profileService.putUserData({firstName: firstName, lastName: lastName, address: address}).then(resp => {
            alert(resp);
        });
        this.setState({readOnly: true});
    }

    buttonPanel = () => {

        if (this.state.readOnly) {
            return (
                <Button type="primary"
                        onClick={ e => {this.startEditing()} }
                        style={{marginTop: '30px'}}>Edit</Button>
            );
        }
        else {
            return (
                <div>
                    <Button type="primary"
                            onClick={ e => {this.saveEditing()} }
                            style={{marginTop: '30px', marginRight: '10px'}}>Save</Button>
                    <Button type="primary"
                            onClick={ e => {this.cancelEditing()} }
                            style={{marginTop: '30px'}}>Cancel</Button>
                </div>
            );
        }

    }

    render() {
        return (
            <div className="profileView">
                {
                    this.state.userData ?
                        <Card className="profileCard"
                              bordered={true}
                              style={{width: 400}}>
                            <div style={{textAlign: 'left'}}>
                                <Meta
                                    avatar={<Avatar src={this.state.userData.avatarURL} />}
                                    title={this.state.userData.fullName} />

                                <br />
                                <br />
                                <strong>email: </strong>
                                <Input defaultValue={this.state.userData.email} disabled={true} />
                                <br/>
                                <br/>
                                <strong>first name: </strong>
                                <Input ref='firstName' defaultValue={this.state.userData.firstName} disabled={this.state.readOnly} />
                                <br/>
                                <br/>
                                <strong>last name: </strong>
                                <Input ref='lastName' defaultValue={this.state.userData.lastName} disabled={this.state.readOnly} />
                                <br/>
                                <br/>
                                <strong>address: </strong>
                                <Input ref='address' defaultValue={this.state.userData.address} disabled={this.state.readOnly} />
                            </div>
                            {this.buttonPanel()}
                        </Card>: null
                }
            </div>
        );
    }

}

export default withRouter(ProfileView);