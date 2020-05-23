import React, { Component } from 'react';
import './style.css';
import "antd/dist/antd.css";
import { Card, Input, Button } from 'antd';
import { profileService } from './ProfileService';


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
        this.loadUserData();
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
        this.refs.email.setState({value: this.state.originalData.email});
        this.refs.address.setState({value: this.state.originalData.address});
        this.setState({readOnly: true});
    }

    saveEditing = () => {
        let email = this.refs.email.state.value;
        let address = this.refs.address.state.value;

        profileService.putUserData({email: email, address: address}).then(resp => {
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
                              title={`${this.state.userData.firstName} ${this.state.userData.secondName}`}
                              bordered={false}
                              style={{width: 300}}>
                            <div style={{textAlign: 'left'}}>
                                <strong>email: </strong>
                                <Input ref='email' defaultValue={this.state.userData.email} disabled={this.state.readOnly} />
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

export default ProfileView;